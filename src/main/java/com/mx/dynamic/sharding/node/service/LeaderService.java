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

    // 选举 leader，所有机器都去创建临时序列节点 /leader/election/latch，以最小的为主
    public void electLeader() {
        nodeStorage.executeInLeader(LeaderNode.ELECTION_LATCH, new LeaderElectionExecutionCallback());
    }

    // 阻塞判断是否是 leader
    // 如果没有 leader，并且在线的实例不为空，就一直循环
    // 循环中如果本机实例也存在，则参加选举，否则一直循环
    // 当有 leader 或者在线的实例为空时，返回本机是否是 leader，即判断 /leader/election/instance 的值是否是本机实例 192.168.1.3@-@14609
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

        // leader 选举结束后回调，如果没有 leader，那么自己去创建临时节点 /leader/election/instance，value 为 192.168.1.3@-@14609
        @Override
        public void execute() {
            if (!hasLeader()) {
                // 只有一个实例会创建成功该临时节点
                nodeStorage.setEphemeralNodeData(LeaderNode.ELECTION_INSTANCE, InstanceManager.getINSTANCE().getServerInstance().getInstanceId());
            }
        }
    }
}
