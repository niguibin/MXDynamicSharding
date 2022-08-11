package com.mx.dynamic.sharding.node.service;

import com.mx.dynamic.sharding.context.InstanceManager;
import com.mx.dynamic.sharding.context.ServerInstance;
import com.mx.dynamic.sharding.node.path.InstancesNode;
import com.mx.dynamic.sharding.node.storage.NodeStorage;
import com.mx.dynamic.sharding.yaml.YamlEngine;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/8/9 4:04 下午
 */
public class InstancesService {

    private final NodeStorage nodeStorage;

    public InstancesService(NodeStorage nodeStorage) {
        this.nodeStorage = nodeStorage;
    }

    public boolean hasInstances() {
        List<String> instances = nodeStorage.getNodeChildrenKeys(InstancesNode.ROOT);
        return !instances.isEmpty();
    }

    public boolean hasLocalInstance() {
        List<String> instances = nodeStorage.getNodeChildrenKeys(InstancesNode.ROOT);
        return instances.contains(InstanceManager.getINSTANCE().getServerInstance().getInstanceId());
    }

    public void persistOnline() {
        nodeStorage.setEphemeralNodeData(InstancesNode.getLocalInstancePath(), InstancesNode.getLocalInstanceValue());
    }

    public List<ServerInstance> getAllInstances() {
        List<ServerInstance> result = new LinkedList<>();
        for (String each : nodeStorage.getNodeChildrenKeys(InstancesNode.ROOT)) {
            ServerInstance instance = YamlEngine.unmarshal(nodeStorage.getNodeData(InstancesNode.getInstancePath(each)), ServerInstance.class);
            if (instance != null) {
                result.add(instance);
            }
        }
        return result;
    }
}
