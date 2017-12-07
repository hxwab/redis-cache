package com.ctrip.db.cache.http;


import com.ctrip.db.cache.compress.DataCompressFactory;
import com.ctrip.db.cache.util.DefaultCacheOptions;
import com.ctrip.db.cache.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 缓存通用控制器处理类
 * Created by zhao.yong on 2017/11/16.
 */
@RestController
@RequestMapping("/cache")
public class CacheCommonController {
    private Logger LOGGER = LoggerFactory.getLogger(HttpCacheManager.class);

    /**
     * 压缩数据
     * @param dataList
     * @return
     */
    @RequestMapping(value="/compressData",method= RequestMethod.POST)
    public Object compressData(@RequestBody List<Object> dataList){
        return  DataCompressFactory.getDataCompressData("gzip",dataList);
    }

    /**
     * 解压数据
     * @param key
     * @param hashKey
     * @return
     */
    @RequestMapping(value="/uncompressData")
    public Object compressData(String key,String hashKey){
        if(StringUtils.isEmpty(hashKey)){
            try {
                byte[] resultData = RedisUtil.get(key.getBytes());
                return DataCompressFactory.getUnCompressData(resultData, "gzip");
            } catch (Exception e) {
                LOGGER.error("解压redis数据错误",e);
            }
        }else{
            byte[] resultData = RedisUtil.hGet(key.getBytes(),hashKey.getBytes());
            return DataCompressFactory.getUnCompressData(resultData, "gzip");
        }
        return null;
    }

    /**
     * 获取所有keys
     * @param keyPattern
     * @return
     */
    @RequestMapping("/getKeys")
    public  Set<String>  getKeys(String keyPattern){
        try {
           return RedisUtil.keys(keyPattern);
        } catch (Exception e) {
            LOGGER.error("调用RedisUtil.keys异常！",e);
        }
        return null;
    }

    /**
     * 获取key的分片
     * @param key
     * @return
     */
    @RequestMapping("/getKeyShard")
    public String getKeyShard(String key){
        int keyOffset =  Math.abs(Objects.hashCode(key)) % DefaultCacheOptions.VIRTUAL_NODE_NUMBER;
        return "shard"+keyOffset;
    }
    /**
     * 删除字符串类型的数据
     * @param keyList
     * @return
     */
    @RequestMapping(value="/deleteStrData",method = RequestMethod.POST)
    public  String  deleteStrData(@RequestBody  List<String> keyList){
        String result = "";
        try {
            long count = RedisUtil.del(keyList.toArray(new String[]{}));
            result = "删除"+count+"个成功！";
            return result;
        } catch (Exception e) {
            LOGGER.error("调用RedisUtil.del异常！",e);
            result = "删除失败!Exception="+e.getMessage();
        }
        return result;
    }

    @RequestMapping(value="/deleteHashData",method = RequestMethod.POST)
    public  String  deleteHashData(@RequestBody  HashDataRequest hashDataRequest){
        String result = "";
        String key = hashDataRequest.getKey();
        List<String> hashKeyList = hashDataRequest.getHashKey();
        int batchSize = hashDataRequest.getBatchSize();
        long delay = hashDataRequest.getDelay();
        try {
                try {
                    RedisUtil.hDel(key, hashKeyList.toArray(new String[]{}));
                    //设置延迟调度
                    if(delay > 0){
                        Thread.sleep(delay);
                    }
                } catch (Exception e) {
                    LOGGER.error("调用RedisUtil.hDel异常！",e);
                }

            result = "删除成功！";
        } catch (Exception e) {
            LOGGER.error("调用RedisUtil.keys异常！",e);
            result = "删除失败!Exception="+e.getMessage();
        }
        return result;
    }
}
