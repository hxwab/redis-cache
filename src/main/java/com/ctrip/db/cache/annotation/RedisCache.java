package com.ctrip.db.cache.annotation;


import com.ctrip.db.cache.util.DefaultCacheOptions;

import java.lang.annotation.*;

/**
 * Redis 缓存注解
 * Created by zhao.yong on 2017/9/27.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface RedisCache {

    /**
     * 缓存的KEY，用于标识key的唯一性，一般是实体的ID作为KEY
     * @return
     */
    String key();

    /**
     * 缓存的实体对象对应的表名，针对存储过程操作或者其他
     * @return
     */
    String tableName() default "";

    /**
     * 采用黑名单排除法排除不缓存的字段
     * @return
     */
    String[] excludeFields() default {};
    /**
     * 缓存的实体类，查询时需要指定，用于反序列化
     * @return
     */
    Class<?> cacheObject() default Object.class;

    /**
     * 过期时间，以秒为单位,默认为0，永不过期
     * @return
     */
    long expireTime() default 0;

    /**
     * 数据存储类型，默认为Hash存储
     * @return
     */
    DefaultCacheOptions.StoreType dataType() default DefaultCacheOptions.StoreType.HASH;

}
