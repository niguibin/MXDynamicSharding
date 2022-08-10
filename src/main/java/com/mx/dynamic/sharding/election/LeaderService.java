package com.mx.dynamic.sharding.election;

import com.mx.dynamic.sharding.base.InstanceManager;
import com.mx.dynamic.sharding.server.InstanceService;
import com.mx.dynamic.sharding.storage.LeaderExecutionCallback;
import com.mx.dynamic.sharding.storage.NodeStorage;
import lombok.RequiredArgsConstructor;

/**
 * @author: niguibin
 * @date: 2022/8/9 2:57 下午
 */
public class LeaderService {

    private final InstanceService instanceService;

    private final NodeStorage nodeStorage;

    public LeaderService() {
        nodeStorage = new NodeStorage();
        instanceService = new InstanceService();
    }

    public void electLeader() {
        nodeStorage.executeInLeader(LeaderNode.LATCH, new LeaderElectionExecutionCallback());
    }

    public boolean isLeaderUntilBlock() {
        while (!hasLeader() && instanceService.hasInstances()) {
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (instanceService.hasLocalInstance()) {
                electLeader();
            }
        }
        return isLeader();
    }

    public boolean isLeader() {
        return InstanceManager.getINSTANCE().getInstance().getInstanceId().equals(nodeStorage.getNodeData(LeaderNode.INSTANCE));
    }

    public boolean hasLeader() {
        return nodeStorage.isNodeExisted(LeaderNode.INSTANCE);
    }

    public void removeLeader() {
        nodeStorage.removeNodeIfExisted(LeaderNode.INSTANCE);
    }

    @RequiredArgsConstructor
    class LeaderElectionExecutionCallback implements LeaderExecutionCallback {

        @Override
        public void execute() {
            if (!hasLeader()) {
                nodeStorage.setEphemeralNodeData(LeaderNode.INSTANCE, InstanceManager.getINSTANCE().getInstance().getInstanceId());
            }
        }
    }
}
