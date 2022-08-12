package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.node.path.InstancesNode;
import com.mx.dynamic.sharding.node.service.ShardingService;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @author: niguibin
 * @date: 2022/8/11 2:58 下午
 */
public class InstancesChangedListener extends AbstractCuratorCacheListener {

    private final ShardingService shardingService;

    public InstancesChangedListener(ShardingService shardingService) {
        this.shardingService = shardingService;
    }

    @Override
    protected void dataChanged(final String path, final CuratorCacheListener.Type eventType, final String data) {
        if (isInstancesChange(eventType, path)) {
            shardingService.setReshardingFlag();
        }
    }

    private boolean isInstancesChange(final CuratorCacheListener.Type eventType, final String path) {
        return InstancesNode.isInstancesPath(path) && CuratorCacheListener.Type.NODE_CHANGED != eventType;
    }
}
