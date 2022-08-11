package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.node.service.InstancesService;
import com.mx.dynamic.sharding.node.service.LeaderService;
import com.mx.dynamic.sharding.node.service.ShardingService;
import com.mx.dynamic.sharding.node.storage.NodeStorage;
import com.mx.dynamic.sharding.notice.ShardingNotice;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @author: niguibin
 * @date: 2022/8/10 4:48 下午
 */
public class ListenerManager {

    private final LeaderService leaderService;

    private final InstancesService instancesService;

    private final ShardingService shardingService;

    private final NodeStorage nodeStorage;

    private final ShardingNotice shardingNotice;

    public ListenerManager(LeaderService leaderService, InstancesService instancesService, ShardingService shardingService, NodeStorage nodeStorage, ShardingNotice shardingNotice) {
        this.leaderService = leaderService;
        this.instancesService = instancesService;
        this.shardingService = shardingService;
        this.nodeStorage = nodeStorage;
        this.shardingNotice = shardingNotice;
    }

    public void addAllListeners() {
        addDataListener(new LeaderElectionListener(leaderService, instancesService));
        addDataListener(new ShardingNecessaryListener(shardingService));
        addDataListener(new ShardingProcessListener(shardingNotice, shardingService));
        addDataListener(new InstancesChangedListener(shardingService));
        nodeStorage.addConnectionStateListener(new RegistryCenterConnectionStateListener(instancesService));
    }

    private void addDataListener(CuratorCacheListener listener) {
        nodeStorage.addDataListener(listener);
    }
}
