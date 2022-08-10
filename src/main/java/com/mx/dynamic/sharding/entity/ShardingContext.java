package com.mx.dynamic.sharding.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author: niguibin
 * @date: 2022/8/10 8:18 下午
 */
@RequiredArgsConstructor
@Getter
@ToString
public class ShardingContext {

    private final int shardingItem;

    private final int shardingTotal;
}
