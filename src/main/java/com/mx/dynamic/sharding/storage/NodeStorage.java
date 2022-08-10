package com.mx.dynamic.sharding.storage;

import com.mx.dynamic.sharding.base.InstanceManager;
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

    public boolean isNodeExisted(String node) {
        return InstanceManager.getINSTANCE().getRegistryCenter().isExisted(NodePath.getFullPath(node));
    }

    public String getNodeData(String node) {
        return InstanceManager.getINSTANCE().getRegistryCenter().get(NodePath.getFullPath(node));
    }

    public String getNodeDataDirectly(String node) {
        return InstanceManager.getINSTANCE().getRegistryCenter().getDirectly(NodePath.getFullPath(node));
    }

    public List<String> getNodeChildrenKeys(String node) {
        return InstanceManager.getINSTANCE().getRegistryCenter().getChildrenKeys(NodePath.getFullPath(node));
    }

    public void createNodeIfNeeded(String node) {
        if (!isNodeExisted(node)) {
            InstanceManager.getINSTANCE().getRegistryCenter().persist(NodePath.getFullPath(node), "");
        }
    }

    public void removeNodeIfExisted(String node) {
        if (isNodeExisted(node)) {
            InstanceManager.getINSTANCE().getRegistryCenter().remove(NodePath.getFullPath(node));
        }
    }

    public void setNodeData(String node, Object value) {
        InstanceManager.getINSTANCE().getRegistryCenter().persist(NodePath.getFullPath(node), value.toString());
    }

    public void setEphemeralNodeData(String node, Object value) {
        InstanceManager.getINSTANCE().getRegistryCenter().persistEphemeral(NodePath.getFullPath(node), value.toString());
    }

    public void updateNodeData(String node, Object value) {
        InstanceManager.getINSTANCE().getRegistryCenter().update(NodePath.getFullPath(node), value.toString());
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
        try (LeaderLatch latch = new LeaderLatch(getClient(), NodePath.getFullPath(latchNode))) {
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
        CuratorCache cache = (CuratorCache) InstanceManager.getINSTANCE().getRegistryCenter().getRawCache();
        cache.listenable().addListener(listener);
    }

    private CuratorFramework getClient() {
        return (CuratorFramework) InstanceManager.getINSTANCE().getRegistryCenter().getRawClient();
    }

    public long getRegistryCenterTime() {
        return InstanceManager.getINSTANCE().getRegistryCenter().getRegistryCenterTime(NodePath.getFullPath("systemTime/current"));
    }
}
