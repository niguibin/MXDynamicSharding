package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.node.service.InstancesService;
import com.mx.dynamic.sharding.node.service.LeaderService;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @author: niguibin
 * @date: 2022/8/11 2:54 下午
 */
public class LeaderElectionListener extends AbstractCuratorCacheListener {

    private final LeaderService leaderService;

    private final InstancesService instancesService;

    public LeaderElectionListener(LeaderService leaderService, InstancesService instancesService) {
        this.leaderService = leaderService;
        this.instancesService = instancesService;
    }

    @Override
    protected void dataChanged(final String path, final CuratorCacheListener.Type eventType, final String data) {
        if (isActiveElection() && instancesService.hasLocalInstance()) {
            leaderService.electLeader();
        }
    }

    // 是否激活选举
    private boolean isActiveElection() {
        // 1. 没有 /{jobName}/leader/election/instance 节点
        // 2. isLocalServerEnabled
        return !leaderService.hasLeader();
    }
}
