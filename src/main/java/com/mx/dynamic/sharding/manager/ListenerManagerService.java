package com.mx.dynamic.sharding.manager;

import com.mx.dynamic.sharding.storage.NodeStorage;

/**
 * @author: niguibin
 * @date: 2022/8/10 4:48 下午
 */
public class ListenerManagerService {

    private NodeStorage nodeStorage;

    private ElectionListenerManager electionListenerManager;

    private ShardingListenerManager shardingListenerManager;

    private RegistryCenterConnectionStateListener registryCenterConnectionStateListener;

    public ListenerManagerService() {
        nodeStorage = new NodeStorage();
        electionListenerManager = new ElectionListenerManager();
        shardingListenerManager = new ShardingListenerManager();
        registryCenterConnectionStateListener = new RegistryCenterConnectionStateListener();
    }

    public void startAllListeners() {
        electionListenerManager.start();

        shardingListenerManager.start();

        nodeStorage.addConnectionStateListener(registryCenterConnectionStateListener);
    }


}
