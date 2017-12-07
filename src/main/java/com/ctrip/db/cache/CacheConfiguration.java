package com.ctrip.db.cache;


import com.ctrip.db.cache.redis.CacheManager;
import com.ctrip.db.cache.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缓存配置类
 * Created by zhao.yong on 2017/10/12.
 */
public class CacheConfiguration {

    private Logger LOGGER = LoggerFactory.getLogger(CacheConfiguration.class);
    public static String CACHE_CONFIG_KEY = "dlt_redis_cache_config";
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @PostConstruct
    public void loadCacheConfig(){
        try {
            if(RedisUtil.existsKey(CACHE_CONFIG_KEY)){
                setLocalMapCache();
            }else{
                Properties properties = PropertiesLoaderUtils.loadAllProperties("cache-config.properties", CacheConfiguration.class.getClassLoader());
                Set<Map.Entry<Object, Object>> entries = properties.entrySet();
                if(!CollectionUtils.isEmpty(entries)){
                    for (Map.Entry<Object,Object> entry : entries){
                        CacheManager.CACHE_MAPPINGS.put(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
                    }
                    RedisUtil.hmset(CACHE_CONFIG_KEY,CacheManager.CACHE_MAPPINGS);
                }
            }
            //开启刷新本地Cache监听
            refreshLocalCache();
        } catch (Exception e) {
            LOGGER.error("加载缓存配置失败",e);
        }
    }

    /**
     * 刷新本地缓存
     */
    private void refreshLocalCache(){
        //开启异步监听
        executor.submit(()->{
            RedisUtil.subscribe(new RedisUtil.MessageListener(){
                @Override
                public void onMessage(String channel, String message) {
                    //刷新本地缓存
                    setLocalMapCache();
                }
            },CACHE_CONFIG_KEY);
        });

    }

    /**
     * 设置本地Map缓存
     */
    private void setLocalMapCache(){
        Map<String,String> map =  RedisUtil.hGetAll(CACHE_CONFIG_KEY);
        if(map != null){
            CacheManager.setCacheMappings(map);
        }
    }
}
