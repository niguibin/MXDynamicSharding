package com.mx.dynamic.sharding.node.service;

import com.mx.dynamic.sharding.context.InstanceManager;
import com.mx.dynamic.sharding.context.ServerInstance;
import com.mx.dynamic.sharding.node.path.LeaderNode;
import com.mx.dynamic.sharding.node.path.ShardingNode;
import com.mx.dynamic.sharding.node.storage.NodeStorage;
import com.mx.dynamic.sharding.node.storage.TransactionExecutionCallback;
import com.mx.dynamic.sharding.utils.BlockUtils;
import lombok.RequiredArgsConstructor;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author: niguibin
 * @date: 2022/8/10 5:03 下午
 */
public class ShardingService {

    private final NodeStorage nodeStorage;

    private final LeaderService leaderService;

    private final InstancesService instanceService;

    public ShardingService(NodeStorage nodeStorage) {
        this.nodeStorage = nodeStorage;
        this.leaderService = new LeaderService(nodeStorage);
        this.instanceService = new InstancesService(nodeStorage);
    }

    // 判断本机是不是 leader，如果是则创建持久化节点 /leader/sharding/necessary，触发 ShardingNecessaryListener 监听器
    // 和 ShardingProcessListener 监听器。只不过在判断是否是 leader 时，还会检查是否有 leader，没有就一直等待或者如果本机实例也在线就去选举
    public void setReshardingFlag() {
        if(!leaderService.isLeaderUntilBlock()) {
            return;
        }
        nodeStorage.createNodeIfNeeded(LeaderNode.SHARDING_NECESSARY);
    }

    public boolean isNeedSharding() {
        return nodeStorage.isNodeExisted(LeaderNode.SHARDING_NECESSARY);
    }

    // 如果有必要就分片，这个方法是当 /instances 下的子节点新增或删除时，由 leader 来执行
    public void shardingIfNecessary() {
        // 获取到在线的所有实例
        List<ServerInstance> instances = instanceService.getAllInstances();
        // 对实例排序
        Collections.reverse(instances);
        // 如果 /leader/sharding/necessary 节点不存在或者在线实例为空则返回
        if (!isNeedSharding() || instances.isEmpty()) {
            return;
        }
        // 如果 /leader/sharding/necessary 节点存在，并且在线实例不为空继续执行

        // 判断是不是 leader，
        if (!leaderService.isLeaderUntilBlock()) {
            blockUntilShardingCompleted();
            return;
        }
        // 获取在线实例总数
        int shardingTotal = instances.size();
        // 创建临时节点 /leader/sharding/processing
        nodeStorage.setEphemeralNodeData(LeaderNode.SHARDING_PROCESS, "");
        resetShardingInfo(shardingTotal);
        Map<ServerInstance, Integer> shardingResult = new HashMap<>();
        for (int i = 0; i < shardingTotal; i++) {
            shardingResult.put(instances.get(i), i);
        }
        // resetShardingInfo 方法中，只是创建了 /sharding/%s 节点
        // 接下来，在事务中，创建持久化节点 /sharding/%s/instance，并赋值
        nodeStorage.executeInTransaction(new PersistShardingInfoTransactionExecutionCallback(shardingResult));
    }

    // 如果不是 leader
    // 并且存在 /leader/sharding/necessary 节点或存在 /leader/sharding/processing 节点就一直阻塞，这个条件下，说明分片还没有完成
    private void blockUntilShardingCompleted() {
        while(!leaderService.isLeaderUntilBlock() && (isNeedSharding() || nodeStorage.isNodeExisted(LeaderNode.SHARDING_PROCESS))) {
            // 阻塞直到分片完成
            BlockUtils.waitingShortTime();
        }
    }

    // 重新分片
    // 遍历 shardingTotal，shardingTotal 是 /instances 下的子节点总数，即在线实例的总数，移除 /sharding/%s/instance 节点，然后再创建持久化节点 /sharding/%s
    // 遍历结束之后，获取 /sharding 节点的子节点的个数，如果子节点个数大于本次总分片个数，则说明实例减少了，/sharding 节点先还有部分没有删除，
    // 继续删除大于本次总分片的 /sharding 子节点
    private void resetShardingInfo(int shardingTotal) {
        for (int i = 0; i < shardingTotal; i++) {
            nodeStorage.removeNodeIfExisted(ShardingNode.getInstancePath(i));
            nodeStorage.createNodeIfNeeded(ShardingNode.getItemPath(i));
        }
        int actualShardingTotal = nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT).size();
        if (actualShardingTotal > shardingTotal) {
            for (int i = shardingTotal; i< actualShardingTotal; i++) {
                nodeStorage.removeNodeIfExisted(ShardingNode.getItemPath(i));
            }
        }
    }

    @RequiredArgsConstructor
    static class PersistShardingInfoTransactionExecutionCallback implements TransactionExecutionCallback {

        private final Map<ServerInstance, Integer> shardingResult;

        // 创建 /sharding/%s/instance 节点，并设置值为 /instances 下的对应节点值转化为 ServerInstance 对象的 instanceId
        // 然后删除 /leader/sharding/necessary 和 /leader/sharding/processing 节点，让非 leader 实例结束
        // blockUntilShardingCompleted 中的阻塞，同时触发 ShardingProcessListener 监听器，执行 ShardingNotice#shardingCompleted 方法，
        // 通知分片完成
        @Override
        public List<CuratorOp> createCuratorOperators(TransactionOp transactionOp) throws Exception {
            List<CuratorOp> result = new LinkedList<>();
            for (Map.Entry<ServerInstance, Integer> entry : shardingResult.entrySet()) {
                result.add(transactionOp.create().forPath(ShardingNode.getInstancePath(entry.getValue()), entry.getKey().getInstanceId().getBytes(StandardCharsets.UTF_8)));
            }
            result.add(transactionOp.delete().forPath(LeaderNode.SHARDING_NECESSARY));
            result.add(transactionOp.delete().forPath(LeaderNode.SHARDING_PROCESS));
            return result;
        }
    }

    // 如果 /instances 节点下没有本机实例 192.168.1.3@-@14609 节点，则返回 null
    // 如果有的话，则先获取 /sharding 节点的子节点（是一个数字，代表分片结果中的值），然后遍历获取 /sharding/%s/instance 节点的值，
    // 看哪个是本机实例 192.168.1.3@-@14609，则返回那个数字
    public Integer getShardingItem(String instanceId) {
        if (!instanceService.hasLocalInstance()) {
            return null;
        }
        List<String> items = nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT);
        for (int i = 0; i < items.size(); i++) {
            String itemInstanceId = nodeStorage.getNodeData(ShardingNode.getInstancePath(i));
            if (instanceId.equals(itemInstanceId)) {
                return i;
            }
        }
        return null;
    }

    public Integer getLocalShardingItem() {
        return getShardingItem(InstanceManager.getINSTANCE().getServerInstance().getInstanceId());
    }

    // 如果 /instances 节点下没有本机实例 192.168.1.3@-@14609 节点，则返回空集合
    // 如果有的话，返回 /sharding 节点下的子节点，调用方只使用子节点的个数
    public List<String> getAllShardingItems() {
        if (!instanceService.hasLocalInstance()) {
            return Collections.emptyList();
        }
        return nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT);
    }

    public boolean hasShardingInfo() {
        return !nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT).isEmpty();
    }
}
