package com.mx.dynamic.sharding.facade;

import com.mx.dynamic.sharding.election.LeaderService;
import com.mx.dynamic.sharding.manager.ListenerManagerService;
import com.mx.dynamic.sharding.server.InstanceService;
import com.mx.dynamic.sharding.server.ReconcileService;

/**
 * @author: niguibin
 * @date: 2022/8/9 2:55 下午
 */
public class SetUpFacade {

    private LeaderService leaderService;

    private InstanceService instanceService;

    private ReconcileService reconcileService;

    private ListenerManagerService listenerManagerService;

    public SetUpFacade() {
        leaderService = new LeaderService();
        instanceService = new InstanceService();
        reconcileService = new ReconcileService();
        listenerManagerService = new ListenerManagerService();
    }

    public void registerStartUpInfo() {
        listenerManagerService.startAllListeners();

        leaderService.electLeader();

        instanceService.persistOnline();

        if (!reconcileService.isRunning()) {
            reconcileService.startAsync();
        }
    }
}
