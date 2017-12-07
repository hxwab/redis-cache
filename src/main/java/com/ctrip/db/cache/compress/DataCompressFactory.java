package com.ctrip.db.cache.compress;

import com.ctrip.db.cache.compress.io.GZipDataCompress;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhao.yong on 2017/11/8.
 */
public class DataCompressFactory {

    private DataCompressFactory(){}

    /**
     * 获取压缩的数据
     * @param compressType
     * @param dataObject
     * @return
     */
    public static byte[] getDataCompressData(String compressType,Object dataObject){
        DataPersistCompress dataPersistCompress = getDataPersistCompress(compressType);
        return dataPersistCompress.compress(dataObject);
    }

    /**
     * 获取解压缩的数据
     * @param data
     * @param compressType
     * @return
     */
    public static  String getUnCompressData(byte[] data,String compressType){
        if(data == null){
            return  null;
        }
        DataPersistCompress dataPersistCompress = getDataPersistCompress(compressType);
        return dataPersistCompress.uncompress(data,String.class);
    }

    /**
     * @param bytes
     * @param compressType
     * @return
     */
    public static List<String> getUnCompressData(Collection<byte[]> bytes, String compressType){
        if(CollectionUtils.isEmpty(bytes)){
            return  null;
        }
        List<String> dataList = new LinkedList<>();
        for(byte[] data : bytes){
            dataList.add(getUnCompressData(data,compressType));
        }
        return dataList;
    }
    /**
     * 通过压缩算法获取指定的压缩服务接口
     * @param compressType
     * @return
     */
    private static DataPersistCompress getDataPersistCompress(String compressType){
        if(StringUtils.isEmpty(compressType)){
            return new GZipDataCompress();
        }
        CompressAlgorithmType compressAlgorithmType = CompressAlgorithmType.getCompressAlgorithmType(compressType);
        if(compressAlgorithmType == null){
           throw new RuntimeException("当前的压缩算法"+compressType+"不支持，目前仅支持这些算法："+CompressAlgorithmType.getAllCompressAlgorithm());
        }
        DataPersistCompress dataPersistCompress = null;
        switch (compressAlgorithmType){
            case GZIP:
                dataPersistCompress = new GZipDataCompress();
                break;
            default:
                //默认采用GZIP进行数据压缩
                dataPersistCompress = new GZipDataCompress();
                break;
        }
        return dataPersistCompress;
    }
}
