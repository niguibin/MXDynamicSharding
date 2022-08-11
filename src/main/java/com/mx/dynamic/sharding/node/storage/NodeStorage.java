package com.mx.dynamic.sharding.node.storage;

import com.mx.dynamic.sharding.registry_center.CoordinatorRegistryCenter;
import com.mx.dynamic.sharding.exception.RegException;
import com.mx.dynamic.sharding.exception.RegExceptionHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionStateListener;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/8/9 3:02 下午
 */
public class NodeStorage {

    private final CoordinatorRegistryCenter registryCenter;

    public NodeStorage(CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    public boolean isNodeExisted(String node) {
        return registryCenter.isExisted(node);
    }

    public String getNodeData(String node) {
        return registryCenter.get(node);
    }

    public String getNodeDataDirectly(String node) {
        return registryCenter.getDirectly(node);
    }

    public List<String> getNodeChildrenKeys(String node) {
        return registryCenter.getChildrenKeys(node);
    }

    public void createNodeIfNeeded(String node) {
        if (!isNodeExisted(node)) {
            registryCenter.persist(node, "");
        }
    }

    public void removeNodeIfExisted(String node) {
        if (isNodeExisted(node)) {
            registryCenter.remove(node);
        }
    }

    public void setNodeData(String node, Object value) {
        registryCenter.persist(node, value.toString());
    }

    public void setEphemeralNodeData(String node, Object value) {
        registryCenter.persistEphemeral(node, value.toString());
    }

    public void updateNodeData(String node, Object value) {
        registryCenter.update(node, value.toString());
    }

    public void executeInTransaction(TransactionExecutionCallback callback) {
        try {
            List<CuratorOp> operations = new LinkedList<>();
            CuratorFramework client = getClient();
            TransactionOp transactionOp = client.transactionOp();
            operations.add(transactionOp.check().forPath("/"));
            operations.addAll(callback.createCuratorOperators(transactionOp));
            client.transaction().forOperations(operations);
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
        }
    }

    public void executeInLeader(String latchNode, LeaderExecutionCallback callback) {
        try (LeaderLatch latch = new LeaderLatch(getClient(), latchNode)) {
            latch.start();
            latch.await();
            callback.execute();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else {
            throw new RegException(e);
        }
    }

    public void addConnectionStateListener(ConnectionStateListener listener) {
        getClient().getConnectionStateListenable().addListener(listener);
    }

    public void addDataListener(CuratorCacheListener listener) {
        CuratorCache cache = (CuratorCache) registryCenter.getRawCache();
        cache.listenable().addListener(listener);
    }

    private CuratorFramework getClient() {
        return (CuratorFramework) registryCenter.getRawClient();
    }

    public long getRegistryCenterTime() {
        return registryCenter.getRegistryCenterTime("/systemTime/current");
    }
}
