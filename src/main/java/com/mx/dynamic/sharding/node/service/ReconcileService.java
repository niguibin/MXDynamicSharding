package com.mx.dynamic.sharding.node.service;

import com.google.common.util.concurrent.AbstractScheduledService;

import java.util.concurrent.TimeUnit;

/**
 * @author: niguibin
 * @date: 2022/8/10 7:54 下午
 */
public class ReconcileService extends AbstractScheduledService {

    private long lastReconcileTime;

    private ShardingService shardingService;

    public ReconcileService(ShardingService shardingService) {
        lastReconcileTime = System.currentTimeMillis();
        this.shardingService = shardingService;
    }

    @Override
    protected void runOneIteration() throws Exception {
        int reconcileIntervalMinutes = 30;
        if (reconcileIntervalMinutes > 0 && (System.currentTimeMillis() - lastReconcileTime) >= reconcileIntervalMinutes * 60 * 1000) {
            lastReconcileTime = System.currentTimeMillis();
            if (!shardingService.isNeedSharding() && !shardingService.hasShardingInfo()) {
                shardingService.setReshardingFlag();
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
    }
}
