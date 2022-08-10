package com.mx.dynamic.sharding.base;

import com.mx.dynamic.sharding.entity.Instance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author: niguibin
 * @date: 2022/8/10 3:17 下午
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstanceManager {

    private static volatile InstanceManager INSTANCE;

    private CoordinatorRegistryCenter registryCenter;

    private Instance instance;

    private Integer shardingTotal;

    public static InstanceManager getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (InstanceManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InstanceManager();
                }
            }
        }
        return INSTANCE;
    }

    public CoordinatorRegistryCenter getRegistryCenter() {
        return registryCenter;
    }

    public void setRegistryCenter(CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Instance getInstance() {
        return instance;
    }

    public Integer getShardingTotal() {
        return shardingTotal;
    }

    public void setShardingTotal(Integer shardingTotal) {
        this.shardingTotal = shardingTotal;
    }
}
