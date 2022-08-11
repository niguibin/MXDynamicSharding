package com.mx.dynamic.sharding.node.path;

import com.mx.dynamic.sharding.context.InstanceManager;
import com.mx.dynamic.sharding.yaml.YamlEngine;

/**
 * @author: niguibin
 * @date: 2022/8/11 11:16 上午
 */
public class InstancesNode {

    public static final String ROOT = "/instances";

    public static final String INSTANCE = ROOT + "/%s";

    public static boolean isInstancesPath(String path) {
        return path.startsWith(ROOT);
    }

    public static String getLocalInstancePath() {
        return getInstancePath(InstanceManager.getINSTANCE().getServerInstance().getInstanceId());
    }

    public static String getLocalInstanceValue() {
        return YamlEngine.marshal(InstanceManager.getINSTANCE().getServerInstance());
    }

    public static String getInstancePath(String instanceId) {
        return String.format(INSTANCE, instanceId);
    }
}
