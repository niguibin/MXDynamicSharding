package com.mx.dynamic.sharding.base;

/**
 * @author: niguibin
 * @date: 2022/7/29 2:33 下午
 */
public interface ElectionCandidate {

    void startLeadership() throws Exception;

    void stopLeadership() throws Exception;
}
