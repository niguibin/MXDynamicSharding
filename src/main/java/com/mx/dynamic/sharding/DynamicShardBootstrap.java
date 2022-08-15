package com.mx.dynamic.sharding;

import com.google.common.base.Strings;
import com.mx.dynamic.sharding.context.InstanceManager;
import com.mx.dynamic.sharding.context.ServerInstance;
import com.mx.dynamic.sharding.facade.SetUpFacade;
import com.mx.dynamic.sharding.notice.ShardingNotice;
import com.mx.dynamic.sharding.registry_center.zookeeper.ZookeeperConfiguration;
import com.mx.dynamic.sharding.registry_center.zookeeper.ZookeeperRegistryCenter;

/**
 * @author: niguibin
 * @date: 2022/8/9 2:48 下午
 */
public class DynamicShardBootstrap {

    private final SetUpFacade setUpFacade;

    public DynamicShardBootstrap(ZookeeperConfiguration zkConfig, ShardingNotice shardingNotice) {
        checkParams(zkConfig);
        InstanceManager.getINSTANCE().setServerInstance(new ServerInstance());
        ZookeeperRegistryCenter registryCenter = new ZookeeperRegistryCenter(zkConfig);
        registryCenter.init();
        this.setUpFacade = new SetUpFacade(registryCenter, shardingNotice);
    }

    public void start() {
        this.setUpFacade.start();
    }

    public void checkParams(ZookeeperConfiguration zkConfig) {
        if (Strings.isNullOrEmpty(zkConfig.getNamespace())) {
            throw new IllegalArgumentException("namespace is not null");
        }
    }
}
