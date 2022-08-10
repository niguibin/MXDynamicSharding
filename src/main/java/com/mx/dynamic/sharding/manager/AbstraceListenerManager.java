package com.mx.dynamic.sharding.manager;

import com.mx.dynamic.sharding.storage.NodeStorage;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @author: niguibin
 * @date: 2022/8/10 4:41 下午
 */
public abstract class AbstraceListenerManager {

    private final NodeStorage nodeStorage;

    protected AbstraceListenerManager() {
        nodeStorage = new NodeStorage();
    }

    public abstract void start();

    protected void addDataListener(CuratorCacheListener listener) {
        nodeStorage.addDataListener(listener);
    }
}
