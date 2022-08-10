package com.mx.dynamic.sharding.storage;

import lombok.RequiredArgsConstructor;

/**
 * @author: niguibin
 * @date: 2022/8/9 3:03 下午
 */
@RequiredArgsConstructor
public class NodePath {

    public static final String LEADER_HOST_NODE = "leader/election/instance";

    public static final String SERVERS_NODE = "servers";

    public static final String INSTANCES_NODE = "instances";

    public static final String SHARDING_NODE = "sharding";

    public static String getFullPath(String node) {
        return String.format("/%s", node);
    }

    public static String getLeaderHostNodePath() {
        return getFullPath(LEADER_HOST_NODE);
    }

    public static String getServersNodePath() {
        return getFullPath(SERVERS_NODE);
    }

    public static String getInstancesNodePath() {
        return getFullPath(INSTANCES_NODE);
    }

    public static String getInstancesNodePath(String instanceId) {
        return String.format("%s/%s", getInstancesNodePath(), instanceId);
    }

    public static String getShardingNodePath() {
        return getFullPath(SHARDING_NODE);
    }

    public static String getShardingNodePath(String item, String nodeName) {
        return String.format("%s/%s/%s", getShardingNodePath(), item, nodeName);
    }
}
