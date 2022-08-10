package com.mx.dynamic.sharding.test;

import com.mx.dynamic.sharding.listener.LeaderElectionListener;
import com.mx.dynamic.sharding.zookeeper.ZookeeperConfiguration;
import com.mx.dynamic.sharding.zookeeper.ZookeeperRegistryCenter;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.zookeeper.server.quorum.Leader;

/**
 * @author: niguibin
 * @date: 2022/8/10 11:11 上午
 */
public class Test {
    public static void main(String[] args) {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("127.0.0.1:2181", "dynamic-sharding");
        ZookeeperRegistryCenter registryCenter = new ZookeeperRegistryCenter(zkConfig);
        registryCenter.init();
        CuratorCache cache = (CuratorCache) registryCenter.getRawCache();
        cache.listenable().addListener(new LeaderElectionListener());
        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
