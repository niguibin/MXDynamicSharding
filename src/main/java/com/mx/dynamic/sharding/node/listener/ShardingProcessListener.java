package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.context.ShardingContext;
import com.mx.dynamic.sharding.node.path.LeaderNode;
import com.mx.dynamic.sharding.node.service.ShardingService;
import com.mx.dynamic.sharding.notice.ShardingNotice;

import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/8/11 7:54 下午
 */
public class ShardingProcessListener extends AbstractCuratorCacheListener {

    private final ShardingNotice shardingNotice;

    private final ShardingService shardingService;

    public ShardingProcessListener(ShardingNotice shardingNotice, ShardingService shardingService) {
        this.shardingNotice = shardingNotice;
        this.shardingService = shardingService;
    }

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

    private ShardingContext getShardingContext() {
        Integer item = shardingService.getLocalShardingItem();
        List<String> allShardingItems = shardingService.getAllShardingItems();
        if (item == null || allShardingItems.size() == 0) {
            return null;
        }
        return new ShardingContext(item, allShardingItems.size());
    }
}
