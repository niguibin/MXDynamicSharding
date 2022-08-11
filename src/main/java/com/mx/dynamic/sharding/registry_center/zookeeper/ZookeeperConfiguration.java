package com.mx.dynamic.sharding.registry_center.zookeeper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author: niguibin
 * @date: 2022/7/29 4:00 下午
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class ZookeeperConfiguration {

    private final String serverLists;

    private final String namespace;

    private int baseSleepTimeMilliseconds = 1000;

    private int maxSleepTimeMilliseconds = 3000;

    private int maxRetries = 3;

    private int sessionTimeoutMilliseconds;

    private int connectionTimeoutMilliseconds;

    private String digest;
}
