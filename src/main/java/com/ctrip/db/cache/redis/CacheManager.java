package com.ctrip.db.cache.redis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理器
 * Created by zhao.yong on 2017/10/24.
 */
public class CacheManager {
    public static Map<String,String> CACHE_MAPPINGS = new ConcurrentHashMap<>();

    public static final CacheManager INSTANCE = new CacheManager();

    public static void setCacheMappings(Map<String,String> cacheMappings){
        CACHE_MAPPINGS = cacheMappings;
    }
}
