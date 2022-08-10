package com.mx.dynamic.sharding.zookeeper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mx.dynamic.sharding.base.CoordinatorRegistryCenter;
import com.mx.dynamic.sharding.exception.RegExceptionHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author: niguibin
 * @date: 2022/8/8 2:34 下午
 */
public class ZookeeperRegistryCenter implements CoordinatorRegistryCenter {

    private final ZookeeperConfiguration zkConfig;

    private CuratorCache cache;

    private CuratorFramework client;

    public ZookeeperConfiguration getZkConfig() {
        return zkConfig;
    }
    public CuratorFramework getClient() {
        return client;
    }

    public ZookeeperRegistryCenter(ZookeeperConfiguration zkConfig) {
        this.zkConfig = zkConfig;
    }

    @Override
    public void init() {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zkConfig.getServerLists())
                .retryPolicy(new ExponentialBackoffRetry(zkConfig.getBaseSleepTimeMilliseconds(), zkConfig.getMaxRetries(), zkConfig.getMaxSleepTimeMilliseconds()))
                .namespace(zkConfig.getNamespace());
        if (zkConfig.getSessionTimeoutMilliseconds() != 0) {
            builder.sessionTimeoutMs(zkConfig.getSessionTimeoutMilliseconds());
        }
        if (zkConfig.getConnectionTimeoutMilliseconds() != 0) {
            builder.connectionTimeoutMs(zkConfig.getConnectionTimeoutMilliseconds());
        }
        if (!Strings.isNullOrEmpty(zkConfig.getDigest())) {
            builder.authorization("digest", zkConfig.getDigest().getBytes(StandardCharsets.UTF_8))
                    .aclProvider(new ACLProvider() {
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(String s) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        client = builder.build();
        client.start();
        try {
            if (!client.blockUntilConnected(zkConfig.getMaxSleepTimeMilliseconds() * zkConfig.getMaxRetries(), TimeUnit.MILLISECONDS)) {
                client.close();
                throw new KeeperException.OperationTimeoutException();
            }
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        cache = CuratorCache.build(client, "/");
        cache.start();
    }

    @Override
    public void close() {
        cache.close();
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }

    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String get(String key) {
        Optional<ChildData> resultInCache = cache.get(key);
        return resultInCache.map(v -> v.getData() == null ? null : new String(v.getData(), StandardCharsets.UTF_8)).orElseGet(() -> getDirectly(key));
    }

    @Override
    public String getDirectly(String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
            return null;
        }
    }

    @Override
    public List<String> getChildrenKeys(String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            result.sort(Comparator.reverseOrder());
            return result;
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getNumChildren(String key) {
        try {
            Stat stat = client.checkExists().forPath(key);
            if (stat != null) {
                return stat.getNumChildren();
            }
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        return 0;
    }

    @Override
    public boolean isExisted(String key) {
        try {
            return client.checkExists().forPath(key) != null;
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
            return false;
        }
    }

    /**
     * 创建持久节点，如果不存在则创建，如果存在则更新
     */
    @Override
    public void persist(String key, String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    /**
     * 更新
     */
    @Override
    public void update(String key, String value) {
        try {
            TransactionOp transactionOp = client.transactionOp();
            client.transaction().forOperations(transactionOp.check().forPath(key), transactionOp.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    /**
     * 创建临时节点
     */
    @Override
    public void persistEphemeral(String key, String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    /**
     * 创建持久序列节点
     */
    @Override
    public String persistSequential(String key, String value) {
        try {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        return null;
    }

    /**
     * 创建临时序列节点
     */
    @Override
    public void persistEphemeralSequential(String key) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    @Override
    public Object getRawCache() {
        return cache;
    }

    @Override
    public void remove(String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    @Override
    public long getRegistryCenterTime(String key) {
        long result = 0L;
        try {
            persist(key, "");
            result = client.checkExists().forPath(key).getMtime();
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
        Preconditions.checkState(result != 0L, "Cannot get registry center time");
        return result;
    }

    @Override
    public Object getRawClient() {
        return client;
    }
}
