package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.node.service.InstancesService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @author: niguibin
 * @date: 2022/8/10 7:43 下午
 * @description: 连接状态监听器
 */
public final class RegistryCenterConnectionStateListener implements ConnectionStateListener {


    private final InstancesService instanceService;

    public RegistryCenterConnectionStateListener(InstancesService instanceService) {
        this.instanceService = instanceService;
    }

    // 如果状态为 RECONNECTED，即断开重新连接（因为刚启动时已经在 SetUpFacade#start 方法中执行了 InstancesService#persistOnline 方法）了
    // 之后，则创建临时节点 /instances/192.168.1.3@-@14609，值为如下内容
    // instanceId: 192.168.1.3@-@14609
    // serverIp: 192.168.1.3
    // 因为是临时节点如果下线了，就断开，就会触发 LeaderElectionListener 和 InstancesChangedListener 监听器
    // LeaderElectionListener 会判断有没有 leader，如果没有，证明下线的是 leader，则需要重新选举；如果有的话，则不做任何处理
    // InstancesChangedListener 也会去检查是否有 leader，如果没有就进行选举或等待
    @Override
    public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
        if (ConnectionState.RECONNECTED == newState) {
            instanceService.persistOnline();
        }
    }
}
