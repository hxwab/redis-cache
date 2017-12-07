package com.ctrip.db.cache.http;

import java.io.Serializable;

/**
 * 缓存操作实体
 * Created by zhao.yong on 2017/10/24.
 */
public class CacheOperateEntity implements Serializable {

    /**
     * 缓存接口操作名
     */
    private String operateName;

    /**
     * 缓存配置项
     */
    private String cacheConfig;
    public String getOperateName() {
        return operateName;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName;
    }

    public String getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(String cacheConfig) {
        this.cacheConfig = cacheConfig;
    }
}
