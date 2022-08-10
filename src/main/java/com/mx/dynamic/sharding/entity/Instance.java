package com.mx.dynamic.sharding.entity;

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
public class Instance {

    public static final String DELIMITER = "@-@";

    private String instanceId;

    private String labels;

    private String serverIp;

    public Instance() {
        this(IpUtils.getIp() + DELIMITER + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }

    public Instance(String instanceId) {
        this(instanceId, null);
    }

    public Instance(String instanceId, String labels) {
        this(instanceId, labels, IpUtils.getIp());
    }

    public Instance(String instanceId, String labels, String serverIp) {
        this.instanceId = instanceId;
        this.labels = labels;
        this.serverIp = serverIp;
    }
}
