package com.mx.dynamic.sharding.node.service;

import com.mx.dynamic.sharding.context.InstanceManager;
import com.mx.dynamic.sharding.node.path.LeaderNode;
import com.mx.dynamic.sharding.node.storage.LeaderExecutionCallback;
import com.mx.dynamic.sharding.node.storage.NodeStorage;
import com.mx.dynamic.sharding.utils.BlockUtils;
import lombok.RequiredArgsConstructor;

/**
 * @author: niguibin
 * @date: 2022/8/9 2:57 下午
 */
public class LeaderService {

    private final InstancesService instanceService;

    private final NodeStorage nodeStorage;

    public LeaderService(NodeStorage nodeStorage) {
        this.nodeStorage = nodeStorage;
        this.instanceService = new InstancesService(nodeStorage);
    }

    public void electLeader() {
        nodeStorage.executeInLeader(LeaderNode.ELECTION_LATCH, new LeaderElectionExecutionCallback());
    }

    public boolean isLeaderUntilBlock() {
        while (!hasLeader() && instanceService.hasInstances()) {
            BlockUtils.waitingShortTime();
            if (instanceService.hasLocalInstance()) {
                electLeader();
            }
        }
        return isLeader();
    }

    public boolean isLeader() {
        return InstanceManager.getINSTANCE().getServerInstance().getInstanceId().equals(nodeStorage.getNodeData(LeaderNode.ELECTION_INSTANCE));
    }

    public boolean hasLeader() {
        return nodeStorage.isNodeExisted(LeaderNode.ELECTION_INSTANCE);
    }

    public void removeLeader() {
        nodeStorage.removeNodeIfExisted(LeaderNode.ELECTION_INSTANCE);
    }

    @RequiredArgsConstructor
    class LeaderElectionExecutionCallback implements LeaderExecutionCallback {

        @Override
        public void execute() {
            if (!hasLeader()) {
                nodeStorage.setEphemeralNodeData(LeaderNode.ELECTION_INSTANCE, InstanceManager.getINSTANCE().getServerInstance().getInstanceId());
            }
        }
    }
}
