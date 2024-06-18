package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.node.service.InstancesService;
import com.mx.dynamic.sharding.node.service.LeaderService;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @author: niguibin
 * @date: 2022/8/11 2:54 下午
 * @description: Leader 选举监听器
 */
public class LeaderElectionListener extends AbstractCuratorCacheListener {

    private final LeaderService leaderService;

    private final InstancesService instancesService;

    public LeaderElectionListener(LeaderService leaderService, InstancesService instancesService) {
        this.leaderService = leaderService;
        this.instancesService = instancesService;
    }

    // 1. 判断是否有 leader：看有没有 /leader/election/instance 节点
    // 2. 判断是否有本机实例：获取 /instances 节点下的子节点，判断是否包含本机实例节点 192.168.1.3@-@14609
    // 3. 如果没有 leader，并且本机实例在 /instances 节点下，那么就进行选举
    @Override
    protected void dataChanged(final String path, final CuratorCacheListener.Type eventType, final String data) {
        if (!leaderService.hasLeader() && instancesService.hasLocalInstance()) {
            leaderService.electLeader();
        }
    }
}
