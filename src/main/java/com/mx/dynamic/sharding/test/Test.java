package com.mx.dynamic.sharding.test;

import com.mx.dynamic.sharding.DynamicShardBootstrap;
import com.mx.dynamic.sharding.context.ShardingContext;
import com.mx.dynamic.sharding.notice.ShardingNotice;
import com.mx.dynamic.sharding.registry_center.zookeeper.ZookeeperConfiguration;

/**
 * @author: niguibin
 * @date: 2022/8/10 11:11 上午
 */
public class Test {
    public static void main(String[] args) {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("127.0.0.1:2181", "dynamic-sharding");
        DynamicShardBootstrap bootstrap = new DynamicShardBootstrap(zkConfig, new ShardingNotice() {
            @Override
            public void startSharding() {
                System.out.println("开始分片");
            }

            @Override
            public void shardingCompleted(ShardingContext shardingContext) {
                System.out.println("结束分片, 分片结果为: " + shardingContext);
            }
        });
        bootstrap.start();
        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
