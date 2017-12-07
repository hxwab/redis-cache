package com.ctrip.db.cache.http;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhao.yong on 2017/11/16.
 */
public class HashDataRequest implements Serializable {

    /**
     * redis key
     */
    private String key;
    /**
     * redis hashkey 列表
     */
    private List<String> hashKey;
    /**
     * 每个批次的延迟时间，默认是0不延迟
     */
    private long delay = 0;
    /**
     * 批次的大小，默认是500
     */
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
