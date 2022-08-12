package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.node.service.InstancesService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @author: niguibin
 * @date: 2022/8/10 7:43 下午
 */
public final class RegistryCenterConnectionStateListener implements ConnectionStateListener {


    private final InstancesService instanceService;

    public RegistryCenterConnectionStateListener(InstancesService instanceService) {
        this.instanceService = instanceService;
    }

    @Override
    public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
        if (ConnectionState.RECONNECTED == newState) {
            instanceService.persistOnline();
        }
    }
}
