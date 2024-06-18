package com.mx.dynamic.sharding.node.listener;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.nio.charset.StandardCharsets;

/**
 * @author: niguibin
 * @date: 2022/8/10 11:48 上午
 * @description: 节点监听器
 */
public abstract class AbstractCuratorCacheListener implements CuratorCacheListener {
    @Override
    public void event(Type type, ChildData oldData, ChildData newData) {
        if (newData == null && oldData == null) {
            return;
        }
        String path = type == Type.NODE_DELETED ? oldData.getPath() : newData.getPath();
        byte[] data = type == Type.NODE_DELETED ? oldData.getData() : newData.getData();
        if (path.isEmpty()) {
            return;
        }
        dataChanged(path, type, data == null ? "" : new String(data, StandardCharsets.UTF_8));
    }

    protected abstract void dataChanged(String path, Type type, String data);
}
