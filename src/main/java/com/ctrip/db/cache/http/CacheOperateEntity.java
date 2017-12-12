package com.ctrip.db.cache.http;

import com.ctrip.db.cache.util.DefaultCacheOptions;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 缓存操作实体
 * Created by zhao.yong on 2017/10/24.
 */
@ApiModel(value="缓存操作对象")
public class CacheOperateEntity implements Serializable {

    @ApiModelProperty(value="缓存配置Key",required = false,notes = "缓存配置Key,hash存储结构key")
    private String cacheKey = DefaultCacheOptions.DEFAULT_CACHE_CONFIG_KEY;
    /**
     * 缓存接口操作名
     */
    @ApiModelProperty(value="缓存DAO接口全路径",required = true,notes = "缓存DAO接口全路径")
    private String operateName;

    /**
     * 缓存配置项
     */
    @ApiModelProperty(value="缓存DAO接口配置",required = true,notes = "缓存DAO接口配置")
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

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }
}
