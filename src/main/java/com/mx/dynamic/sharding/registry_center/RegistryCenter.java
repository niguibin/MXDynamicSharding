package com.mx.dynamic.sharding.registry_center;

/**
 * @author: niguibin
 * @date: 2022/7/29 2:29 下午
 */
public interface RegistryCenter {

    void init();

    void close();

    String get(String key);

    boolean isExisted(String key);

    void persist(String key, String value);

    void update(String key, String value);

    void remove(String key);

    long getRegistryCenterTime(String key);

    Object getRawClient();
}
