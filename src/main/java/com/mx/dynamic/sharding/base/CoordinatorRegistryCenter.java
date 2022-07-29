package com.mx.dynamic.sharding.base;

import java.util.List;

/**
 * @author: niguibin
 * @date: 2022/7/29 2:29 下午
 */
public interface CoordinatorRegistryCenter extends RegistryCenter {

    String getDirectly(String key);
    
    List<String> getChildrenKeys(String key);
    
    int getNumChildren(String key);
    
    void persistEphemeral(String key, String value);
    
    String persistSequential(String key, String value);
    
    void persistEphemeralSequential(String key);
    
    void addCacheData(String cachePath);
    
    void evictCacheData(String cachePath);
    
    Object getRawCache(String cachePath);
}
