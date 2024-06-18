package com.mx.dynamic.sharding.context;

import com.mx.dynamic.sharding.utils.IpUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.lang.management.ManagementFactory;

/**
 * @author: niguibin
 * @date: 2022/8/9 4:32 下午
 */
@Getter
@Setter
@EqualsAndHashCode(of = "instanceId")
public class ServerInstance {

    public static final String DELIMITER = "@-@";

    private String instanceId;

    private String labels;

    private String serverIp;

    public ServerInstance() {
        // 本机实例 id
        // 192.168.1.3@-@14609
        this(IpUtils.getIp() + DELIMITER + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }

    public ServerInstance(String instanceId) {
        this(instanceId, null);
    }

    public ServerInstance(String instanceId, String labels) {
        this(instanceId, labels, IpUtils.getIp());
    }

    public ServerInstance(String instanceId, String labels, String serverIp) {
        this.instanceId = instanceId;
        this.labels = labels;
        this.serverIp = serverIp;
    }
}
