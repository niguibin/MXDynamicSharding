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
        // 1. 如果 /{jobName}/instances 节点下创建或删除了子节点 或 path 是 /{jobName}/servers/{ip} 节点
        if (isInstancesChange(eventType, path)) {
            // 判断是否是 leader，如果是，则创建 /{jobName}/leader/sharding/necessary 节点
            shardingService.setReshardingFlag();
        }
    }

    private boolean isInstancesChange(final CuratorCacheListener.Type eventType, final String path) {
        // 1. 如果 path 是以 /{jobName}/instances 开头并且不是改变事件，即要么是创建，要么是删除
        return InstancesNode.isInstancesPath(path) && CuratorCacheListener.Type.NODE_CHANGED != eventType;
    }
}
