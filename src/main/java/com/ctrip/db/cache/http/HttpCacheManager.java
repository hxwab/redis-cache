package com.ctrip.db.cache.http;

import com.alibaba.fastjson.JSON;
import com.ctrip.db.cache.CacheConfiguration;
import com.ctrip.db.cache.redis.CacheManager;
import com.ctrip.db.cache.util.DefaultCacheOptions;
import com.ctrip.db.cache.util.RedisUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhao.yong on 2017/10/24.
 */
@Api(description = "缓存管理和维护相关操作")
@RestController
@RequestMapping("/cache")
public class HttpCacheManager {

    private Logger LOGGER = LoggerFactory.getLogger(HttpCacheManager.class);

    @ApiOperation(value="增加接口缓存操作",notes = "增加接口缓存操作,指的是DAO接口",nickname = "zhao.yong")
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public String addCacheOperate(@RequestBody CacheOperateEntity cacheOperateEntity){
        //CacheManager.CACHE_MAPPINGS.put(cacheOperateEntity.getOperateName(),cacheOperateEntity.getCacheConfig());
        if(StringUtils.isEmpty(cacheOperateEntity.getCacheKey())){
            cacheOperateEntity.setCacheKey(DefaultCacheOptions.DEFAULT_CACHE_CONFIG_KEY);
        }
        //同步缓存
        syncCacheConfig(cacheOperateEntity, OperateType.SAVEORUPDATE);
        return "Success";
    }

    @ApiOperation(value="删除接口缓存操作",notes = "删除接口缓存操作,指的是DAO接口",nickname = "zhao.yong")
    @RequestMapping(value="/delete",method = RequestMethod.POST)
    public String deleteCacheOperate(@RequestBody CacheOperateEntity cacheOperateEntity){
        CacheManager.CACHE_MAPPINGS.remove(cacheOperateEntity.getOperateName());
        if(StringUtils.isEmpty(cacheOperateEntity.getCacheKey())){
            cacheOperateEntity.setCacheKey(DefaultCacheOptions.DEFAULT_CACHE_CONFIG_KEY);
        }
        //同步缓存
        syncCacheConfig(cacheOperateEntity, OperateType.DELETE);
        return "Success";
    }

    @ApiOperation(value="查询接口缓存配置",notes = "查询接口缓存配置,指的是DAO接口",nickname = "zhao.yong")
    @RequestMapping(value="/query",method = RequestMethod.GET)
    public Object queryCacheOperate(String operateName){
        if(StringUtils.hasText(operateName)){
            return CacheManager.CACHE_MAPPINGS.get(operateName);
        }else{
           return JSON.toJSONString(CacheManager.CACHE_MAPPINGS,true);
        }
    }

    private enum OperateType{
        SAVEORUPDATE,
        DELETE
    }

    /**
     * @param cacheOperateEntity
     * @param operateType
     */
    private void syncCacheConfig(CacheOperateEntity cacheOperateEntity,OperateType operateType){
        try {
            if(OperateType.SAVEORUPDATE.equals(operateType)){
                RedisUtil.hSet(cacheOperateEntity.getCacheKey(), cacheOperateEntity.getOperateName(), cacheOperateEntity.getCacheConfig());
            }else if(OperateType.DELETE.equals(operateType)){
                RedisUtil.hDel(cacheOperateEntity.getCacheKey(),cacheOperateEntity.getOperateName());
            }
            //刷新各个应用本地缓存
            RedisUtil.publish(CacheConfiguration.CACHE_CONFIG_KEY,"refresh_local_cache");

        } catch (Exception e) {
            LOGGER.error("同步Redis数据失败",e);
        }
    }
}
