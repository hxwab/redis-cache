package com.ctrip.db.cache.http;

import com.alibaba.fastjson.JSON;
import com.ctrip.db.cache.CacheConfiguration;
import com.ctrip.db.cache.redis.CacheManager;
import com.ctrip.db.cache.util.RedisUtil;
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
@RestController
@RequestMapping("/cache")
public class HttpCacheManager {

    private Logger LOGGER = LoggerFactory.getLogger(HttpCacheManager.class);
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public String addCacheOperate(@RequestBody CacheOperateEntity cacheOperateEntity){
        CacheManager.CACHE_MAPPINGS.put(cacheOperateEntity.getOperateName(),cacheOperateEntity.getCacheConfig());
        //同步缓存
        syncCacheConfig(cacheOperateEntity, OperateType.SAVEORUPDATE);
        return "Success";
    }

    @RequestMapping(value="/delete",method = RequestMethod.POST)
    public String deleteCacheOperate(@RequestBody CacheOperateEntity cacheOperateEntity){
        CacheManager.CACHE_MAPPINGS.remove(cacheOperateEntity.getOperateName());
        //同步缓存
        syncCacheConfig(cacheOperateEntity, OperateType.DELETE);
        return "Success";
    }

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

    private void syncCacheConfig(CacheOperateEntity cacheOperateEntity,OperateType operateType){
        try {
            if(OperateType.SAVEORUPDATE.equals(operateType)){
                RedisUtil.hSet(CacheConfiguration.CACHE_CONFIG_KEY,cacheOperateEntity.getOperateName(),cacheOperateEntity.getCacheConfig());
            }else if(OperateType.DELETE.equals(operateType)){
                RedisUtil.hDel(CacheConfiguration.CACHE_CONFIG_KEY,cacheOperateEntity.getOperateName());
            }

        } catch (Exception e) {
            LOGGER.error("同步Redis数据失败",e);
        }
    }
}
