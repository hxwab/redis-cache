package com.ctrip.db.cache.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
}
