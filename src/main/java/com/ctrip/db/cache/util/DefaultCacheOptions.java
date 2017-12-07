package com.ctrip.db.cache.util;

/**
 * 默认缓存选项
 */
public final class DefaultCacheOptions {
    /**
     * 分片的数据，默认100个分片
     */
    public static  final  int VIRTUAL_NODE_NUMBER = 100;
    /**
     * 在指定的时间窗口，默认并发请求数1000
     */
    public static  final  int MAX_REQUESTS = 1000;
    /**
     * 默认的时间窗口为1分钟，以秒为单位
     */
    public static  final  int TIME_WINDOW = 60;

    /**
     * 限流缓存过期时间
     */
    public static  final  long LIMIT_CACHE_EXPIRE = 10*60;

    /**
     * 限流策略
     */
    public enum LimitStrategy{
        BASED_REQUEST,BASED_SECOND_CACHE;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    /**
     * Redis 数据存储类型
     */
    public enum StoreType{
        STRING, HASH, LIST, SET, SORTSET;
        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}
