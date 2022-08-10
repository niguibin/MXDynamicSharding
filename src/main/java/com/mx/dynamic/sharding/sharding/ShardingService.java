package com.mx.dynamic.sharding.sharding;

import com.mx.dynamic.sharding.base.InstanceManager;
import com.mx.dynamic.sharding.election.LeaderService;
import com.mx.dynamic.sharding.entity.Instance;
import com.mx.dynamic.sharding.server.InstanceNode;
import com.mx.dynamic.sharding.server.InstanceService;
import com.mx.dynamic.sharding.storage.NodePath;
import com.mx.dynamic.sharding.storage.NodeStorage;
import com.mx.dynamic.sharding.storage.TransactionExecutionCallback;
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

    private NodeStorage nodeStorage;

    private LeaderService leaderService;

    private InstanceService instanceService;

    private InstanceNode instanceNode;

    public ShardingService() {
        nodeStorage = new NodeStorage();
        leaderService = new LeaderService();
        instanceNode = new InstanceNode();
        instanceService = new InstanceService();
    }

    public void setReshardingFlag() {
        if(!leaderService.isLeaderUntilBlock()) {
            return;
        }
        nodeStorage.createNodeIfNeeded(ShardingNode.NECESSARY);
    }

    public boolean isNeedSharding() {
        return nodeStorage.isNodeExisted(ShardingNode.NECESSARY);
    }

    public void shardingIfNecessary() {
        List<Instance> instances = instanceService.getAllInstances();
        if (!isNeedSharding() || instances.isEmpty()) {
            return;
        }
        if (!leaderService.isLeaderUntilBlock()) {
            blockUntilShardingCompleted();
            return;
        }
        int shardingTotal = instances.size();
        nodeStorage.setEphemeralNodeData(ShardingNode.PROCESSING, "");
        resetShardingInfo(shardingTotal);
        Map<Instance, Integer> shardingResult = new HashMap<>();
        for (int i = 0; i < shardingTotal; i++) {
            shardingResult.put(instances.get(i), i);
        }

        nodeStorage.executeInTransaction(new PersistShardingInfoTransactionExecutionCallback(shardingResult));
    }
    
    private void blockUntilShardingCompleted() {
        while(!leaderService.isLeaderUntilBlock() && (isNeedSharding() || nodeStorage.isNodeExisted(ShardingNode.PROCESSING))) {
            BlockUtils.waitingShortTime();
        }
    }
    
    private void resetShardingInfo(int shardingTotal) {
        for (int i = 0; i < shardingTotal; i++) {
            nodeStorage.removeNodeIfExisted(ShardingNode.getInstanceNode(i));
            nodeStorage.createNodeIfNeeded(ShardingNode.ROOT + "/" + i);
        }
        int actualShardingTotal = nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT).size();
        if (actualShardingTotal > shardingTotal) {
            for (int i = shardingTotal; i< actualShardingTotal; i++) {
                nodeStorage.removeNodeIfExisted(ShardingNode.ROOT + "/" + i);
            }
        }
    }

    @RequiredArgsConstructor
    class PersistShardingInfoTransactionExecutionCallback implements TransactionExecutionCallback {

        private final Map<Instance, Integer> shardingResult;

        @Override
        public List<CuratorOp> createCuratorOperators(TransactionOp transactionOp) throws Exception {
            List<CuratorOp> result = new LinkedList<>();
            for (Map.Entry<Instance, Integer> entry : shardingResult.entrySet()) {
                result.add(transactionOp.create().forPath(NodePath.getFullPath(ShardingNode.getInstanceNode(entry.getValue())), entry.getKey().getInstanceId().getBytes(StandardCharsets.UTF_8)));
            }
            result.add(transactionOp.delete().forPath(NodePath.getFullPath(ShardingNode.NECESSARY)));
            result.add(transactionOp.delete().forPath(NodePath.getFullPath(ShardingNode.PROCESSING)));
            return result;
        }
    }

    public Integer getShardingItem(String instanceId) {
        if (instanceService.hasLocalInstance()) {
            return -1;
        }
        List<String> items = nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT);
        items.sort(Comparator.reverseOrder());
        for (int i = 0; i < items.size(); i++) {
            String itemData = nodeStorage.getNodeData(ShardingNode.ROOT + "/" + i);
            if (instanceId.equals(itemData)) {
                return i;
            }
        }
        return -1;
    }

    public Integer getLocalShardingItem() {
        return getShardingItem(InstanceManager.getINSTANCE().getInstance().getInstanceId());
    }

    public List<String> getAllShardingItems() {
        if (instanceService.hasLocalInstance()) {
            return Collections.emptyList();
        }
        List<String> items = nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT);
        items.sort(Comparator.reverseOrder());
        return items;
    }

    public boolean hasShardingInfo() {
        return !nodeStorage.getNodeChildrenKeys(ShardingNode.ROOT).isEmpty();
    }
}
