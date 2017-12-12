package com.ctrip.db.cache.util;

import com.ctrip.db.cache.CacheConfiguration;
import credis.java.client.CacheProvider;
import credis.java.client.util.CacheFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "db.cache.redis")
public class CacheConfigProperties {
    /**
     * 缓存配置Key
     */
    private String configKeyName;
    /**
     * Redis集群的名称
     */
    private String clusterName;

    public String getConfigKeyName() {
        return configKeyName;
    }

    public void setConfigKeyName(String configKeyName) {
        this.configKeyName = configKeyName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String toString() {
        return "CacheConfigProperties{" +
                "configKeyName='" + configKeyName + '\'' +
                ", clusterName='" + clusterName + '\'' +
                '}';
    }

    /**
     * 初始化缓存提供者
     */
    @PostConstruct
    public void initCacheProvider(){
        if(clusterName == null){
            throw new RuntimeException("Redis缓存同步框架没有配置[db.cache.redis.cluster-name]集群名称，请在cache-sync.properties或者application.properties文件进行配置!");
        }
        CacheProvider cacheProvider = CacheFactory.GetProvider(clusterName);
        RedisUtil.setCacheProvider(cacheProvider);
        if(!StringUtils.isEmpty(configKeyName)){
            CacheConfiguration.CACHE_CONFIG_KEY = configKeyName;
        }
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        //加载缓存配置
        cacheConfiguration.loadCacheConfig();
    }
}
