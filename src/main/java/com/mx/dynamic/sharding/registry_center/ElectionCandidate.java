package com.mx.dynamic.sharding.registry_center;

/**
 * @author: niguibin
 * @date: 2022/7/29 2:33 下午
 */
public interface ElectionCandidate {

    void startLeadership() throws Exception;

    void stopLeadership() throws Exception;
}
