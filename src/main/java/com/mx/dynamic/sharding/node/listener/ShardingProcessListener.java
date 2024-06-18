package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.context.ShardingContext;
import com.mx.dynamic.sharding.node.path.LeaderNode;
import com.mx.dynamic.sharding.node.service.ShardingService;
import com.mx.dynamic.sharding.notice.ShardingNotice;

import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/8/11 7:54 下午
 * @description: 分片通知监听器，开始时和完成后都发出通知
 * 其实可以与 ShardingNecessaryListener 放一起，因为都是监听了 /leader/sharding/necessary 节点
 * 但是与 ShardingNecessaryListener 分开，可以避免如果在 ShardingNotice#startSharding 方法中阻塞了，
 * 那么 ShardingService#shardingIfNecessary 方法也就阻塞了，所以分开比较好
 */
public class ShardingProcessListener extends AbstractCuratorCacheListener {

    private final ShardingNotice shardingNotice;

    private final ShardingService shardingService;

    public ShardingProcessListener(ShardingNotice shardingNotice, ShardingService shardingService) {
        this.shardingNotice = shardingNotice;
        this.shardingService = shardingService;
    }

    // 1. 如果变化的节点是 /leader/sharding/necessary
    // 2. 如果变化类型是创建，则调用 ShardingNotice#startSharding 方法
    // 3. 如果变化类型是删除，则已经创建完成，调用 ShardingNotice#shardingCompleted 完成方法
    @Override
    protected void dataChanged(String path, Type type, String data) {
        if (isShardingProcessPath(path)) {
            if (type == Type.NODE_CREATED) {
                shardingNotice.startSharding();
            } else if (type == Type.NODE_DELETED) {
                shardingNotice.shardingCompleted(getShardingContext());
            }
        }
    }

    private boolean isShardingProcessPath(String path) {
        return LeaderNode.isShardingNecessaryPath(path);
    }

    // 可以看出来最后通知到的 ShardingContext 可能为 null，所以实现 ShardingContext 时需要判空
    private ShardingContext getShardingContext() {
        Integer item = shardingService.getLocalShardingItem();
        List<String> allShardingItems = shardingService.getAllShardingItems();
        if (item == null || allShardingItems.size() == 0) {
            return null;
        }
        return new ShardingContext(item, allShardingItems.size());
    }
}
