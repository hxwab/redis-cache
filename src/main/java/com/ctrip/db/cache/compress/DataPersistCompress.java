package com.ctrip.db.cache.compress;

/**
 * 数据持久化压缩存储接口
 * Created by zhao.yong on 2017/11/8.
 */
public interface DataPersistCompress {

    /**
     * 数据压缩
     * @param object
     */
    byte[] compress(Object object);

    /**
     * 数据解压缩
     * @param data   二进制字节码数据
     * @param  targetClass
     * @param <T>
     * @return
     */
   <T> T uncompress(byte[] data, Class<T> targetClass);
}
