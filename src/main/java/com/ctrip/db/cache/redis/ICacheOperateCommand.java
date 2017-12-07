package com.ctrip.db.cache.redis;


import com.ctrip.db.cache.annotation.RedisCacheProperty;

import java.util.List;

/**
 * 缓存操作命令接口
 * Created by zhao.yong on 2017/9/30.
 */
public interface ICacheOperateCommand {
    /**
     * 保存数据到缓存
     * @param cachePropertyList
     * @param paramObject
     */
     void saveCacheData(List<RedisCacheProperty> cachePropertyList, Object paramObject) throws Exception;

    /**
     * 更新缓存数据
     * @param cacheProperty
     * @param paramObject
     */
     void updateCacheData(RedisCacheProperty cacheProperty, Object paramObject) throws Exception;

    /**
     * 查询缓存数据
     * @param cacheProperty
     * @return
     */
     Object queryCacheData(RedisCacheProperty cacheProperty);

    /**
     * 删除缓存数据
     * @param cachePropertyList
     */
     void deleteCacheData(List<RedisCacheProperty> cachePropertyList) throws Exception;
}
