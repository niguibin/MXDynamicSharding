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

    // 如果变化的节点是 /instances 开头的，即 /instances 节点或其子节点的话
    // 并且变化类型不是 NODE_CHANGED，那么就是创建或者删除时，此时判断本机是不是 leader，如果是则创建 /leader/sharding/necessary 节点，
    // 触发 ShardingNecessaryListener 监听器和 ShardingProcessListener 监听器。
    // 只不过在判断是否是 leader 时，还会检查是否有 leader，没有就一直等待或者如果本机实例也在线就去选举
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
