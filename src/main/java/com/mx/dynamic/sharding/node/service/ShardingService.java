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

    public void setReshardingFlag() {
        if(!leaderService.isLeaderUntilBlock()) {
            return;
        }
        nodeStorage.createNodeIfNeeded(LeaderNode.SHARDING_NECESSARY);
    }

    public boolean isNeedSharding() {
        return nodeStorage.isNodeExisted(LeaderNode.SHARDING_NECESSARY);
    }

    public void shardingIfNecessary() {
        List<ServerInstance> instances = instanceService.getAllInstances();
        Collections.reverse(instances);
        if (!isNeedSharding() || instances.isEmpty()) {
            return;
        }
        if (!leaderService.isLeaderUntilBlock()) {
            blockUntilShardingCompleted();
            return;
        }
        int shardingTotal = instances.size();
        nodeStorage.setEphemeralNodeData(LeaderNode.SHARDING_PROCESS, "");
        resetShardingInfo(shardingTotal);
        Map<ServerInstance, Integer> shardingResult = new HashMap<>();
        for (int i = 0; i < shardingTotal; i++) {
            shardingResult.put(instances.get(i), i);
        }
        nodeStorage.executeInTransaction(new PersistShardingInfoTransactionExecutionCallback(shardingResult));
    }
    
    private void blockUntilShardingCompleted() {
        while(!leaderService.isLeaderUntilBlock() && (isNeedSharding() || nodeStorage.isNodeExisted(LeaderNode.SHARDING_PROCESS))) {
            BlockUtils.waitingShortTime();
        }
    }
    
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
