package com.ctrip.db.cache.redis;


import com.ctrip.db.cache.annotation.RedisCacheProperty;
import com.ctrip.db.cache.util.optimize.SecondLevelCacheStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Redis 操作代理类
 * Created by zhao.yong on 2017/9/30.
 */
public class RedisOperateProxy {

    private Logger LOGGER=LoggerFactory.getLogger(RedisOperateProxy.class);
    private static final String TAGS = "[[title=缓存同步]] %s 操作失败！";
    /**
     * 采用线程池异步操作Redis
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private ICacheOperateCommand redisOperateCommand = RedisOperateCommand.getInstance();

    /**
     * 保存一级缓存数据
     * @param cachePropertyList
     * @param paramObject
     */
    public void saveRedisData(List<RedisCacheProperty> cachePropertyList, Object paramObject){
        executorService.execute(()->{
            try {
                redisOperateCommand.saveCacheData(cachePropertyList,paramObject);
            } catch (Exception e) {
                LOGGER.error(String.format(TAGS,"saveRedisData"),e);
            }
        });
    }

    /**
     * 保存二级缓存数据
     * @param cacheProperty
     * @param paramObject
     */
    public void saveSecondCacheData(RedisCacheProperty cacheProperty,Object paramObject){
        RedisCacheProperty targetCache = new RedisCacheProperty();
        BeanUtils.copyProperties(cacheProperty,targetCache);
        SecondLevelCacheStrategy.setDataType(targetCache);
        SecondLevelCacheStrategy.setExpireTime(targetCache);
        saveRedisData(Arrays.asList(cacheProperty),paramObject);
    }

    public void updateRedisData(RedisCacheProperty cacheProperty,Object paramObject){
        executorService.execute(()->{
            try {
                redisOperateCommand.updateCacheData(cacheProperty,paramObject);
            } catch (Exception e) {
                LOGGER.error(String.format(TAGS,"updateRedisData"),e);
            }
        });
    }

    /**
     * 查询一级缓存数据
     * @param cacheProperty
     * @return
     */
    public Object queryRedisData(RedisCacheProperty cacheProperty){
        Object resultData = redisOperateCommand.queryCacheData(cacheProperty);
        return resultData;
    }

    /**
     * 查询二级缓存数据
     * @param cacheProperty
     * @return
     */
    public Object querySecondCacheData(RedisCacheProperty cacheProperty){
        RedisCacheProperty targetCache = new RedisCacheProperty();
        BeanUtils.copyProperties(cacheProperty,targetCache);
        SecondLevelCacheStrategy.setDataType(targetCache);
        return queryRedisData(targetCache);
    }

    public void deleteRedisData(List<RedisCacheProperty> cachePropertyList){
        executorService.execute(()->{
            try {
                redisOperateCommand.deleteCacheData(cachePropertyList);
            } catch (Exception e) {
                LOGGER.error(String.format(TAGS,"deleteRedisData"),e);
            }
        });
    }

}
