package com.ctrip.db.cache.util;

import com.ctrip.db.cache.intercept.RedisCacheIntercepter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 通用工具类
 */
public class CommonUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);
    /**
     * SHA-1 Hash摘要算法
     */
   private static MessageDigest messageDigest;
   static {
       try {
           messageDigest  = MessageDigest.getInstance("SHA-1");
       } catch (NoSuchAlgorithmException e) {
           LOGGER.warn("没有SHA-1这种hash摘要算法",e);
       }
   }

    /**
     * 获取对象的HashCode编码
     * @param inputData
     * @return
     */
   public synchronized static long getHashCode(Object inputData){
       if(inputData == null ){
           throw new RuntimeException("CommonUtils。getHashCode 输入数据不能为Null");
       }
       try {
           byte[] digest = messageDigest.digest(String.valueOf(inputData).getBytes("UTF-8"));
           long rv = ((long)(digest[3]&0xff) << 24)
                   | ((long)(digest[2]&0xff) << 16)
                   | ((long)(digest[1]&0xff) << 8)
                   | ((long)(digest[0]&0xff));
           return rv;
       } catch (UnsupportedEncodingException e) {
           LOGGER.error("不支持UTF-8编码",e);
       }
       return 0;
   }
}
