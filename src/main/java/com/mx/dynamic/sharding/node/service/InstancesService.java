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

    // 判断是否有本机实例，即 /instances 节点下是否有本机实例 192.168.1.3@-@14609 节点
    public boolean hasLocalInstance() {
        List<String> instances = nodeStorage.getNodeChildrenKeys(InstancesNode.ROOT);
        return instances.contains(InstanceManager.getINSTANCE().getServerInstance().getInstanceId());
    }

    // 创建临时节点 /instances/192.168.1.3@-@14609，值为如下内容
    // instanceId: 192.168.1.3@-@14609
    // serverIp: 192.168.1.3
    public void persistOnline() {
        nodeStorage.setEphemeralNodeData(InstancesNode.getLocalInstancePath(), InstancesNode.getLocalInstanceValue());
    }

    // 获取 /instances 节点下的所有子节点（即服务启动时或断开重新连接时调用上面的 persistOnline 方法创建的节点，然后将所有
    // 子节点的 value 通过 YamlEngine 反序列化为 ServerInstance 对象，添加到集合中返回
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
