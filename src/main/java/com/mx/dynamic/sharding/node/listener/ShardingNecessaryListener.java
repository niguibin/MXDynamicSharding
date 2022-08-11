package com.mx.dynamic.sharding.node.listener;

import com.mx.dynamic.sharding.node.path.LeaderNode;
import com.mx.dynamic.sharding.node.service.ShardingService;

/**
 * @author: niguibin
 * @date: 2022/8/11 3:33 下午
 */
public class ShardingNecessaryListener extends AbstractCuratorCacheListener {

    private final ShardingService shardingService;

    public ShardingNecessaryListener(ShardingService shardingService) {
        this.shardingService = shardingService;
    }

    @Override
    protected void dataChanged(String path, Type type, String data) {
        if (isShardingNessaryPath(path) && type == Type.NODE_CREATED) {
            shardingService.shardingIfNecessary();
        }
    }

    private boolean isShardingNessaryPath(String path) {
        return LeaderNode.isShardingNecessaryPath(path);
    }
}
