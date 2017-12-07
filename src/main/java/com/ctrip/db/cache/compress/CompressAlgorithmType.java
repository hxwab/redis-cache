package com.ctrip.db.cache.compress;

import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 数据压缩算法类型
 * Created by zhao.yong on 2017/11/8.
 */
public enum  CompressAlgorithmType {
    GZIP("gzip");

    /**
     * 算法类型
     */
    private String algorithmType;

     CompressAlgorithmType(String algorithmType){
        this.algorithmType = algorithmType;
    }

    /**
     * 获取指定的压缩类型
     * @param typeName
     * @return
     */
    public  static CompressAlgorithmType getCompressAlgorithmType(String typeName){
        CompressAlgorithmType[] values = CompressAlgorithmType.values();
        for (CompressAlgorithmType type: values) {
            if(type.getAlgorithmType().equals(typeName)){
                return type;
            }
        }
        return null;
    }

    /**
     * 获取所有的压缩算法
     * @return
     */
    public static String getAllCompressAlgorithm(){
        CompressAlgorithmType[] values = CompressAlgorithmType.values();
        List<String> typeList = new LinkedList<>();
        for (CompressAlgorithmType type: values) {
            typeList.add(type.getAlgorithmType());
        }
        return StringUtils.collectionToDelimitedString(typeList,",");
    }
    public String getAlgorithmType() {
        return algorithmType;
    }




}
