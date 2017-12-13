package com.ctrip.db.cache.http;


import com.ctrip.db.cache.compress.DataCompressFactory;
import com.ctrip.db.cache.util.CommonUtils;
import com.ctrip.db.cache.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 缓存通用控制器处理类
 * Created by zhao.yong on 2017/11/16.
 */
@RestController
@RequestMapping("/cache")
@Api(description="缓存常用通用工具")
public class CacheCommonController {
    private Logger LOGGER = LoggerFactory.getLogger(HttpCacheManager.class);

    /**
     * 压缩数据
     * @param dataList
     * @return
     */
    @ApiOperation(value = "压缩数据",nickname = "zhaoyong")
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
    @ApiOperation(value = "解压缩数据",notes = "解压缩数据",nickname = "zhao.yong")
    @RequestMapping(value="/uncompressData",method = RequestMethod.GET)
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
    @ApiOperation(value = "获取模糊匹配到的Key",notes = "获取模糊匹配到的Key,只能在测试和开发环境使用",nickname = "zhao.yong")
    @RequestMapping(value="/getKeys",method = RequestMethod.GET)
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
    @ApiOperation(value = "获取Key的分片ShardKey",notes = "获取Key的分片ShardKey",nickname = "zhao.yong")
    @RequestMapping(value="/getKeyShard",method = RequestMethod.GET)
    public String getKeyShard(String key,Integer shardNum){
        long keyOffset =  CommonUtils.getHashCode(key) % shardNum;
        return "shard"+keyOffset;
    }
    /**
     * 删除字符串类型的数据
     * @param keyList
     * @return
     */
    @ApiOperation(value = "删除redis中string结构数据",notes = "删除redis中string结构数据",nickname = "zhao.yong")
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

    /**
     * 删除Hash结构的数据
     * @param hashDataRequest
     * @return
     */
    @ApiOperation(value = "删除Hash结构的数据",notes = "删除Hash结构的数据",nickname = "zhao.yong")
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
