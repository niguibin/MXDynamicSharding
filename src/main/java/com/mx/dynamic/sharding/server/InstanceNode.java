package com.mx.dynamic.sharding.server;

import com.mx.dynamic.sharding.base.InstanceManager;
import com.mx.dynamic.sharding.entity.Instance;
import com.mx.dynamic.sharding.storage.NodePath;
import com.mx.dynamic.sharding.utils.IpUtils;
import com.mx.dynamic.sharding.yaml.YamlEngine;

import java.util.regex.Pattern;

/**
 * @author: niguibin
 * @date: 2022/8/9 4:05 下午
 */
public class InstanceNode {

    private static final String SERVERS = NodePath.SERVERS_NODE + "/%s";

    private static final Pattern SERVER_IP_PATTERN = Pattern.compile(NodePath.getServersNodePath() + "/" + IpUtils.IP_REGEX);

    private boolean isServerPath(String path) {
        return SERVER_IP_PATTERN.matcher(path).matches();
    }

    private boolean isLocalServerPath(String path) {
        Instance instance = InstanceManager.getINSTANCE().getInstance();
        return path.equals(NodePath.getFullPath(getServerNode(instance.getServerIp())));
    }

    String getServerNode(String ip) {
        return String.format(SERVERS, ip);
    }




    public static final String ROOT = "instances";

    private static final String INSTANCES = ROOT + "/%s";

    public boolean isInstancePath(String path) {
        return path.startsWith(NodePath.getInstancesNodePath());
    }

    public boolean isLocalInstancePath(String path) {
        return path.equals(NodePath.getInstancesNodePath(InstanceManager.getINSTANCE().getInstance().getInstanceId()));
    }

    public String getLocalInstancePath() {
        return getInstancePath(InstanceManager.getINSTANCE().getInstance().getInstanceId());
    }

    public String getLocalInstanceValue() {
        return YamlEngine.marshal(InstanceManager.getINSTANCE().getInstance());
    }

    public String getInstancePath(final String instanceId) {
        return String.format(INSTANCES, instanceId);
    }
}
