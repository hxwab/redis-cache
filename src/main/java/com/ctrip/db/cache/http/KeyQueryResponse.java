package com.ctrip.db.cache.http;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author zhao.yong
 * @datetime 2018/1/8
 **/
public class KeyQueryResponse {

    @ApiModelProperty(value="当前迭代的游标")
    private String currentCursor;

    /**
     * 返回的数据key列表
     */
    @ApiModelProperty(value="返回的数据key列表")
    private List<String> keyList;

    public KeyQueryResponse() {
    }

    public KeyQueryResponse(String currentCursor, List<String> keyList) {
        this.currentCursor = currentCursor;
        this.keyList = keyList;
    }

    public String getCurrentCursor() {
        return currentCursor;
    }

    public void setCurrentCursor(String currentCursor) {
        this.currentCursor = currentCursor;
    }

    public List<String> getKeyList() {
        return keyList;
    }

    public void setKeyList(List<String> keyList) {
        this.keyList = keyList;
    }
}
