package com.mx.dynamic.sharding.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author: niguibin
 * @date: 2022/8/10 3:17 下午
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstanceManager {

    private static volatile InstanceManager INSTANCE;

    private ServerInstance instance;

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

    public void setServerInstance(ServerInstance instance) {
        this.instance = instance;
    }

    public ServerInstance getServerInstance() {
        return instance;
    }
}
