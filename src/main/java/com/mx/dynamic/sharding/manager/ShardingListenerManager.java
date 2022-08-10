package com.mx.dynamic.sharding.manager;

import com.mx.dynamic.sharding.listener.AbstractCuratorCacheListener;
import com.mx.dynamic.sharding.server.InstanceNode;
import com.mx.dynamic.sharding.sharding.ShardingService;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @author: niguibin
 * @date: 2022/8/10 7:23 下午
 */
public class ShardingListenerManager extends AbstraceListenerManager {
    private final InstanceNode instanceNode;

    private final ShardingService shardingService;

    public ShardingListenerManager() {
        instanceNode = new InstanceNode();
        shardingService = new ShardingService();
    }

    @Override
    public void start() {
        addDataListener(new ListenInstancesChangedJobListener());
    }

    class ListenInstancesChangedJobListener extends AbstractCuratorCacheListener {

        @Override
        protected void dataChanged(final String path, final CuratorCacheListener.Type eventType, final String data) {
            // 1. 如果 /{jobName}/instances 节点下创建或删除了子节点 或 path 是 /{jobName}/servers/{ip} 节点
            if (isInstanceChange(eventType, path) || !shardingService.hasShardingInfo()) {
                // 判断是否是 leader，如果是，则创建 /{jobName}/leader/sharding/necessary 节点
                shardingService.setReshardingFlag();
            }
        }

        private boolean isInstanceChange(final CuratorCacheListener.Type eventType, final String path) {
            // 1. 如果 path 是以 /{jobName}/instances 开头并且不是改变事件，即要么是创建，要么是删除
            return instanceNode.isInstancePath(path) && CuratorCacheListener.Type.NODE_CHANGED != eventType;
        }

    }
}
