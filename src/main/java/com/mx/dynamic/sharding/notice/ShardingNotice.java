package com.mx.dynamic.sharding.notice;

import com.mx.dynamic.sharding.context.ShardingContext;

/**
 * @author: niguibin
 * @date: 2022/8/11 5:16 下午
 */
public interface ShardingNotice {

    void startSharding();

    void shardingCompleted(ShardingContext shardingContext);
}
