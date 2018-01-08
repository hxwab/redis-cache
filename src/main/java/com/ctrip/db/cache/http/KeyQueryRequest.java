package com.ctrip.db.cache.http;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zhao.yong
 * @datetime 2018/1/8
 **/
public class KeyQueryRequest {
    @ApiModelProperty(value="集群GroupId",required = true)
    private Integer groupId;
    @ApiModelProperty(value="模糊匹配的key的模式",required = true)
    private String keyPattern;
    @ApiModelProperty(value="增量扫描的数量，默认为100")
    private Integer scanCount;
    @ApiModelProperty(value="将扫描游标重置为0")
    private Integer reset;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getKeyPattern() {
        return keyPattern;
    }

    public void setKeyPattern(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public Integer getScanCount() {
        return scanCount;
    }

    public void setScanCount(Integer scanCount) {
        this.scanCount = scanCount;
    }

    public Integer getReset() {
        return reset;
    }

    public void setReset(Integer reset) {
        this.reset = reset;
    }
}
