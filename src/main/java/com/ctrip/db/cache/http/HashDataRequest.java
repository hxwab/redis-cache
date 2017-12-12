package com.ctrip.db.cache.http;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhao.yong on 2017/11/16.
 */
@ApiModel("Hash结构数据请求对象")
public class HashDataRequest implements Serializable {

    /**
     * redis key
     */
    @ApiModelProperty(value="Redis 缓存的数据key",required = true,dataType = "String")
    private String key;
    /**
     * redis hashkey 列表
     */
    @ApiModelProperty(value="Redis 缓存的数据hashkey列表",required = true,dataType = "List")
    private List<String> hashKey;
    /**
     * 每个批次的延迟时间，默认是0不延迟
     */
    @ApiModelProperty(value="批次删除的时间延迟,默认不延迟",required = false,dataType = "Long")
    private long delay = 0;
    /**
     * 批次的大小，默认是500
     */
    @ApiModelProperty(value="批次删除数量，默认是500",required = false,dataType = "Integer")
    private int batchSize=500;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getHashKey() {
        return hashKey;
    }

    public void setHashKey(List<String> hashKey) {
        this.hashKey = hashKey;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
