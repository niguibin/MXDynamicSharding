package com.mx.dynamic.sharding.facade;

import com.mx.dynamic.sharding.entity.ShardingContext;
import com.mx.dynamic.sharding.sharding.ShardingService;

import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/8/10 8:17 下午
 */
public class LiteFacade {

    private ShardingService shardingService;

    public LiteFacade() {
        shardingService = new ShardingService();
    }

    public ShardingContext getShardingContext() {
        shardingService.shardingIfNecessary();
        Integer item = shardingService.getLocalShardingItem();
        List<String> allShardingItems = shardingService.getAllShardingItems();
        return new ShardingContext(item, allShardingItems.size());
    }
}
