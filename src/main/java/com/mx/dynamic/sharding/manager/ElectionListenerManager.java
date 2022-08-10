package com.mx.dynamic.sharding.manager;

import com.mx.dynamic.sharding.election.LeaderNode;
import com.mx.dynamic.sharding.election.LeaderService;
import com.mx.dynamic.sharding.listener.AbstractCuratorCacheListener;
import com.mx.dynamic.sharding.server.InstanceService;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @author: niguibin
 * @date: 2022/8/10 4:49 下午
 */
public class ElectionListenerManager extends AbstraceListenerManager {

    private final LeaderNode leaderNode;

    private final LeaderService leaderService;

    private final InstanceService instanceService;

    public ElectionListenerManager() {
        leaderNode = new LeaderNode();
        leaderService = new LeaderService();
        instanceService = new InstanceService();
    }

    @Override
    public void start() {
        addDataListener(new LeaderElectionJobListener());
    }

    class LeaderElectionJobListener extends AbstractCuratorCacheListener {
        @Override
        protected void dataChanged(final String path, final CuratorCacheListener.Type eventType, final String data) {
            if ((isActiveElection() || isPassiveElection(path, eventType)) && instanceService.hasLocalInstance()) {
                leaderService.electLeader();
            }
        }

        // 是否激活选举
        private boolean isActiveElection() {
            // 1. 没有 /{jobName}/leader/election/instance 节点
            // 2. isLocalServerEnabled
            return !leaderService.hasLeader();
        }

        // 是否被动选举
        private boolean isPassiveElection(final String path, final CuratorCacheListener.Type eventType) {
            // 1. leader 是否挂了
            // 2. 是否是有效节点
            return leaderNode.isLeaderInstancePath(path) && CuratorCacheListener.Type.NODE_DELETED == eventType;
        }
    }
}
