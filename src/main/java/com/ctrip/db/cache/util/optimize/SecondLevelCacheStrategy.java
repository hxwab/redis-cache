package com.ctrip.db.cache.util.optimize;


import com.ctrip.db.cache.annotation.RedisCacheProperty;
import com.ctrip.db.cache.util.DefaultCacheOptions;

/**
 * 二级缓存限制策略
 */
public class SecondLevelCacheStrategy {


      public static void setDataType(RedisCacheProperty redisCacheProperty){
         redisCacheProperty.setDataType(DefaultCacheOptions.StoreType.STRING.toString());
    }
    /**
     * 设置二级缓存过期时间
     * @param redisCacheProperty
     */
    public static void setExpireTime(RedisCacheProperty redisCacheProperty){
        redisCacheProperty.setExpireTime(redisCacheProperty.getLimitCacheExpire());
    }
}
