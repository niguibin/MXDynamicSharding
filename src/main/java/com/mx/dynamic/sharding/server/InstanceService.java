package com.mx.dynamic.sharding.server;

import com.mx.dynamic.sharding.base.InstanceManager;
import com.mx.dynamic.sharding.entity.Instance;
import com.mx.dynamic.sharding.storage.NodeStorage;
import com.mx.dynamic.sharding.yaml.YamlEngine;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/8/9 4:04 下午
 */
public class InstanceService {

    private final NodeStorage nodeStorage;

    private final InstanceNode instanceNode;

    public InstanceService() {
        this.nodeStorage = new NodeStorage();
        instanceNode = new InstanceNode();
    }

    public boolean hasInstances() {
        List<String> instances = nodeStorage.getNodeChildrenKeys(InstanceNode.ROOT);
        return !instances.isEmpty();
    }

    public boolean hasLocalInstance() {
        List<String> instances = nodeStorage.getNodeChildrenKeys(InstanceNode.ROOT);
        return instances.contains(InstanceManager.getINSTANCE().getInstance().getInstanceId());
    }

    public void persistOnline() {
        nodeStorage.setEphemeralNodeData(instanceNode.getLocalInstancePath(), instanceNode.getLocalInstanceValue());
    }

    public List<Instance> getAllInstances() {
        List<Instance> result = new LinkedList<>();
        for (String each : nodeStorage.getNodeChildrenKeys(InstanceNode.ROOT)) {
            Instance instance = YamlEngine.unmarshal(nodeStorage.getNodeData(instanceNode.getInstancePath(each)), Instance.class);
            if (instance != null) {
                result.add(instance);
            }
        }
        return result;
    }

}
