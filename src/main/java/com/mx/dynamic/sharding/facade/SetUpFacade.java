package com.mx.dynamic.sharding.facade;

import com.mx.dynamic.sharding.node.listener.ListenerManager;
import com.mx.dynamic.sharding.node.service.InstancesService;
import com.mx.dynamic.sharding.node.service.LeaderService;
import com.mx.dynamic.sharding.node.service.ReconcileService;
import com.mx.dynamic.sharding.node.service.ShardingService;
import com.mx.dynamic.sharding.node.storage.NodeStorage;
import com.mx.dynamic.sharding.notice.ShardingNotice;
import com.mx.dynamic.sharding.registry_center.CoordinatorRegistryCenter;

/**
 * @author: niguibin
 * @date: 2022/8/9 2:55 下午
 */
public class SetUpFacade {

    private final LeaderService leaderService;

    private final InstancesService instancesService;

    private final ReconcileService reconcileService;

    private final ListenerManager listenerManager;

    public SetUpFacade(CoordinatorRegistryCenter registryCenter, ShardingNotice shardingNotice) {
        NodeStorage nodeStorage = new NodeStorage(registryCenter);
        this.leaderService = new LeaderService(nodeStorage);
        this.instancesService = new InstancesService(nodeStorage);
        ShardingService shardingService = new ShardingService(nodeStorage);
        this.reconcileService = new ReconcileService(shardingService);
        this.listenerManager = new ListenerManager(leaderService, instancesService, shardingService, nodeStorage, shardingNotice);
    }

    public void start() {
        listenerManager.addAllListeners();

        leaderService.electLeader();

        instancesService.persistOnline();

//        if (!reconcileService.isRunning()) {
//            reconcileService.startAsync();
//        }
    }
}
