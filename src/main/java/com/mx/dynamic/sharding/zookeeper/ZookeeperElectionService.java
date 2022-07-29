package com.mx.dynamic.sharding.zookeeper;

import com.mx.dynamic.sharding.base.ElectionCandidate;
import com.mx.dynamic.sharding.exception.RegException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.util.concurrent.CountDownLatch;

/**
 * @author: niguibin
 * @date: 2022/7/29 4:08 下午
 */
public final class ZookeeperElectionService {

    private final CountDownLatch leaderLatch = new CountDownLatch(1);

    private final LeaderSelector leaderSelector;

    public ZookeeperElectionService(final String identity, final CuratorFramework client, final String electionPath, final ElectionCandidate electionCandidate) {
        leaderSelector = new LeaderSelector(client, electionPath, new LeaderSelectorListenerAdapter() {

            @Override
            public void takeLeadership(final CuratorFramework client) throws Exception {
                try {
                    electionCandidate.startLeadership();
                    leaderLatch.await();
                    electionCandidate.stopLeadership();
                } catch (final RegException exception) {
                    System.exit(1);
                }
            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.setId(identity);
    }

    public void start() {
        leaderSelector.start();
    }

    public void stop() {
        leaderLatch.countDown();
        try {
            leaderSelector.close();
        } catch (final Exception ignore) {
        }
    }
}
