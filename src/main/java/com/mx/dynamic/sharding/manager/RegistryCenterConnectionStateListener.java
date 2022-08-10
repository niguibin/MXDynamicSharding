package com.mx.dynamic.sharding.manager;

import com.mx.dynamic.sharding.server.InstanceService;
import com.mx.dynamic.sharding.sharding.ShardingService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @author: niguibin
 * @date: 2022/8/10 7:43 下午
 */
public final class RegistryCenterConnectionStateListener implements ConnectionStateListener {


    private final InstanceService instanceService;

    private final ShardingService shardingService;

    public RegistryCenterConnectionStateListener() {
        instanceService = new InstanceService();
        shardingService = new ShardingService();
    }

    @Override
    public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
        // 如果 zk 连接暂停或者失去连接
        if (ConnectionState.SUSPENDED == newState || ConnectionState.LOST == newState) {
            // 等待恢复
            return;
        } else if (ConnectionState.RECONNECTED == newState) { // 如果 zk 重新连接
            // 设置临时节点 /{jobName}/instances/{jobInstanceId} value 为 jobInstance对象
            instanceService.persistOnline();
        }
    }
}
