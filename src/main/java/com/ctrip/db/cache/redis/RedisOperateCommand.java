package com.ctrip.db.cache.redis;

import com.alibaba.fastjson.JSON;
import com.ctrip.db.cache.annotation.RedisCacheProperty;
import com.ctrip.db.cache.compress.DataCompressFactory;
import com.ctrip.db.cache.util.JSONUtil;
import com.ctrip.db.cache.util.RedisUtil;
import com.ctrip.framework.clogging.agent.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by zhao.yong on 2017/9/28.
 */

public final class RedisOperateCommand implements ICacheOperateCommand{

    private static  final String KEY_SEPERATOR= "_";

    private static final RedisOperateCommand  redisOperateCommand = new RedisOperateCommand();

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisOperateCommand.class);

    private RedisOperateCommand(){

    }

    public static RedisOperateCommand getInstance(){
        return redisOperateCommand;
    }

    /**
     * 保存Redis数据(支持批量插入)
     * @param cachePropertyList
     * @param paramObject
     */
    @Override
    public void saveCacheData(List<RedisCacheProperty> cachePropertyList, Object paramObject) throws Exception {
        //Map类型对象
        if(paramObject instanceof Map){
            Map map = (Map) paramObject;
            //List对象的存储
            if (map.containsKey("collection")){
                List<Object> list = (List<Object>) map.get("collection");
                int index = 0;
                //批量保存到redis
                for (RedisCacheProperty cacheProperty :cachePropertyList ) {
                    RedisCommandOperateDelegate.excuteSetCommand(cacheProperty,JSON.toJSONString(list.get(index)));
                    index++;
                }
            }else if(map.containsKey("param1") && !map.get("param1").getClass().isPrimitive()){
                for (RedisCacheProperty cacheProperty :cachePropertyList ) {
                    RedisCommandOperateDelegate.excuteSetCommand(cacheProperty,JSON.toJSONString(map.get("param1")));
                }
            }
            else{
                //普通Map对象的存储
                for (RedisCacheProperty cacheProperty :cachePropertyList ) {
                    RedisCommandOperateDelegate.excuteSetCommand(cacheProperty,map);
                }
            }
        }else{
            //普通的java对象
            for (RedisCacheProperty cacheProperty :cachePropertyList ){
                RedisCommandOperateDelegate.excuteSetCommand(cacheProperty,paramObject);
            }
        }
    }

    /**
     * 更新Redis缓存数据
     * @param cacheProperty
     * @param paramObject
     */
    @Override
    public void updateCacheData(RedisCacheProperty cacheProperty,Object paramObject) throws Exception {
            Object targetObject = queryCacheData(cacheProperty);
        Object sourceObject = paramObject;
        if(targetObject != null){
            if(targetObject instanceof List){
                List list = (List)targetObject;
                if(list.size() == 0){
                    return;
                }
                targetObject = list.get(0);
            }
            if(paramObject instanceof Map){
                Map map = (Map) paramObject;
                if(map.containsKey("param1") && !map.get("param1").getClass().isPrimitive()){
                    sourceObject = map.get("param1");
                }
            }
            //支持非空字段更新
            BeanUtils.copyProperties(sourceObject,targetObject,getNullProperties(sourceObject));
            saveCacheData(Arrays.asList(cacheProperty),targetObject);
        }

    }

    /**
     * 查询Redis缓存一级数据
     * @param cacheProperty   缓存属性
     * @return
     */
    @Override
    public Object queryCacheData(RedisCacheProperty cacheProperty){
        try {
            return RedisCommandOperateDelegate.excuteGetCommand(cacheProperty);
        } catch (Exception e) {
            LOGGER.error("执行查询缓存数据异常 RedisOperateCommand.queryCacheData",e);
        }
        return null;
    }

    /**
     * 删除Redis缓存数据
     * @param cachePropertyList
     */
    @Override
    public void deleteCacheData(List<RedisCacheProperty> cachePropertyList) {
        for (RedisCacheProperty cacheProperty :cachePropertyList ){
            RedisUtil.hDel(cacheProperty.getCacheKey(),generateCacheKey(cacheProperty));
        }

    }


    /**
     * 获取空的属性值
     * @param source
     * @return
     */
private String[] getNullProperties(Object source){
    Set<String> emptyNames = new HashSet<String>();
    BeanWrapper beanWrapper = new BeanWrapperImpl(source);
    PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
    for (PropertyDescriptor pd : propertyDescriptors){
        Object srcValue = beanWrapper.getPropertyValue(pd.getName());
        if(srcValue == null){
            emptyNames.add(pd.getName());
        }
    }
    return emptyNames.toArray(new String[]{});
}
    /**
     * @param cacheProperty        对应@RedisCache 中的key属性
     * @return
     */
    private static String generateCacheKey(RedisCacheProperty cacheProperty){
        return cacheProperty.getCacheKey()+KEY_SEPERATOR+cacheProperty.getId();
    }

    /**
     * Redis命令操作委托者
     */
    private static class RedisCommandOperateDelegate{
        public static void excuteSetCommand(RedisCacheProperty cacheProperty,Object persistObject) throws Exception {
          String cacheKey = "";
         switch (cacheProperty.getDataType()){
             case "string":
                 //处理in的查询
                 if(cacheProperty.isInConditionClause()) {
                     List dataList = (List) persistObject;
                     String keyProperty = cacheProperty.getId().toString();
                     String[] idList = keyProperty.split(",");
                     Field field = cacheProperty.getTargetClass().getDeclaredField(cacheProperty.getKeyName());
                     field.setAccessible(true);
                     for(String key : idList){
                         RedisCacheProperty singleCacheProperty = new RedisCacheProperty();
                         BeanUtils.copyProperties(cacheProperty,singleCacheProperty);
                         singleCacheProperty.setId(key);
                         Object filterList = dataList.stream().filter(data -> {
                             try {
                                 return key.equals(field.get(data).toString());
                             } catch (Exception e) {
                                 LOGGER.error("通过反射获取字段:{},数据对象:{}错误", cacheProperty.getKeyName(), JSONUtil.toJSONString(data));
                             }
                             return false;
                         }).collect(Collectors.toList());
                         if(filterList!= null && (filterList instanceof List)){
                             //判断是否为压缩存储
                             cacheKey = generateCacheKey(singleCacheProperty);
                             if(cacheProperty.getCompressed()){
                                 RedisUtil.set(cacheKey.getBytes(), DataCompressFactory.getDataCompressData(singleCacheProperty.getCompressType(), filterList));
                             }else{
                                 RedisUtil.set(cacheKey, JSONUtil.toJSONString(filterList, new FastJsonPropertyFilter(singleCacheProperty.getExcludeFields())), String.class);
                             }
                         }
                     }
                 }else{
                     cacheKey = generateCacheKey(cacheProperty);
                     if (cacheProperty.getCompressed()) {
                         RedisUtil.set(cacheKey.getBytes(), DataCompressFactory.getDataCompressData(cacheProperty.getCompressType(), persistObject));
                     } else {
                         RedisUtil.set(cacheKey, JSONUtil.toJSONString(persistObject, new FastJsonPropertyFilter(cacheProperty.getExcludeFields())), String.class);
                     }
                 }

                 break;
             case "list":
                 cacheKey = generateShardCacheKey(cacheProperty);
                 if(cacheProperty.getCompressed()) {
                     RedisUtil.rPush(cacheKey.getBytes(), DataCompressFactory.getDataCompressData(cacheProperty.getCompressType(),persistObject));
                 }else{
                     RedisUtil.rPush(cacheKey, JSONUtil.toJSONString(persistObject, new FastJsonPropertyFilter(cacheProperty.getExcludeFields())));
                 }
                 break;
             case "hash":
                 if(cacheProperty.isInConditionClause()){
                     List dataList = (List) persistObject;
                     String keyProperty = cacheProperty.getId().toString();
                     String[] idList = keyProperty.split(",");
                     Field field = cacheProperty.getTargetClass().getDeclaredField(cacheProperty.getKeyName());
                     field.setAccessible(true);
                     for(String key : idList){
                         RedisCacheProperty singleCacheProperty = new RedisCacheProperty();
                         BeanUtils.copyProperties(cacheProperty,singleCacheProperty);
                         singleCacheProperty.setId(key);
                         Optional optional = dataList.stream().filter(data -> {
                             try {
                                 return key.equals(field.get(data).toString());
                             } catch (Exception e) {
                                 LOGGER.error("通过反射获取字段:{},数据对象:{}错误", cacheProperty.getKeyName(), JSONUtil.toJSONString(data));
                             }
                             return false;
                         }).findFirst();

                         if(optional.isPresent()){
                             Object result = optional.get();
                             cacheKey = generateShardCacheKey(singleCacheProperty);
                             //判断是否为压缩存储
                             if(cacheProperty.getCompressed()){
                                 RedisUtil.hSet(cacheKey.getBytes(), generateCacheKey(singleCacheProperty).getBytes(), DataCompressFactory.getDataCompressData(cacheProperty.getCompressType(),result));
                             }else{
                                 RedisUtil.hSet(cacheKey, generateCacheKey(singleCacheProperty), JSONUtil.toJSONString(result,new FastJsonPropertyFilter(cacheProperty.getExcludeFields())));
                             }
                         }
                     }
                 }else{
                     cacheKey = generateShardCacheKey(cacheProperty);
                     RedisUtil.hSet(cacheKey, generateCacheKey(cacheProperty), JSON.toJSONString(persistObject,new FastJsonPropertyFilter(cacheProperty.getExcludeFields())));
                 }

                 break;
             case "set":
                 cacheKey = generateShardCacheKey(cacheProperty);
                 if(cacheProperty.getCompressed()) {
                     RedisUtil.sadd(cacheKey.getBytes(),DataCompressFactory.getDataCompressData(cacheProperty.getCompressType(),persistObject));
                 }else{
                     RedisUtil.sadd(cacheKey,JSONUtil.toJSONString(persistObject,new FastJsonPropertyFilter(cacheProperty.getExcludeFields())));
                 }
                 break;
             case "sortset":
                 cacheKey = generateShardCacheKey(cacheProperty);
                 double score = RandomUtil.nextDouble() * System.currentTimeMillis();
                 if(cacheProperty.getCompressed()) {
                     RedisUtil.zadd(cacheKey.getBytes(),score, DataCompressFactory.getDataCompressData(cacheProperty.getCompressType(),persistObject));
                 }else{
                     RedisUtil.zadd(cacheKey,score,JSONUtil.toJSONString(persistObject,new FastJsonPropertyFilter(cacheProperty.getExcludeFields())));
                 }

                 break;
            default:
                break;
         }
            //设置key的过期时间
            long expireTime = cacheProperty.getExpireTime();
            if(expireTime > 0){
                RedisUtil.expire(cacheKey,(int)expireTime);
            }
        }

        /**
         * 执行查询缓存
         * @param cacheProperty
         * @return
         */
        public static Object excuteGetCommand(RedisCacheProperty cacheProperty){
            String cacheKey = "";
            String data = "";
            switch (cacheProperty.getDataType()){
                case "string":
                    cacheKey = generateCacheKey(cacheProperty);
                    if(cacheProperty.isCompressed()){
                       byte[] resultData = RedisUtil.get(cacheKey.getBytes());
                        data= DataCompressFactory.getUnCompressData(resultData,cacheProperty.getCompressType());
                    }else{
                        data = RedisUtil.get(cacheKey, String.class);
                    }
                    return getResultData(data,cacheProperty);
                case "list":
                    cacheKey = generateShardCacheKey(cacheProperty);
                    if(cacheProperty.getCompressed()){
                        List<byte[]> bytes = RedisUtil.lRange(cacheKey.getBytes(), 0L, -1L);
                        return DataCompressFactory.getUnCompressData(bytes,cacheProperty.getCompressType());
                    }else{
                        return RedisUtil.lRange(cacheKey,0L,-1L);
                    }
                case "hash":
                    cacheKey = generateShardCacheKey(cacheProperty);
                    if(cacheProperty.isCompressed()){
                        byte[] resultData = RedisUtil.hGet(cacheKey.getBytes(),generateCacheKey(cacheProperty).getBytes());
                        data= DataCompressFactory.getUnCompressData(resultData,cacheProperty.getCompressType());
                    }else {
                        data = RedisUtil.hGet(cacheKey, generateCacheKey(cacheProperty));
                    }
                     return getResultData(data,cacheProperty);
                case "set":
                    cacheKey = generateShardCacheKey(cacheProperty);
                    if(cacheProperty.isCompressed()) {
                        Set<byte[]> bytes = RedisUtil.smembers(cacheKey.getBytes());
                        return DataCompressFactory.getUnCompressData(bytes,cacheProperty.getCompressType());
                    }else {
                        return RedisUtil.smembers(cacheKey);
                    }
                case "sortset":
                    cacheKey = generateShardCacheKey(cacheProperty);
                    if(cacheProperty.isCompressed()) {
                        Set<byte[]> bytes = RedisUtil.zrange(cacheKey.getBytes(),0L,-1L);
                        return DataCompressFactory.getUnCompressData(bytes,cacheProperty.getCompressType());
                    }else {
                        return RedisUtil.zrange(cacheKey,0L,-1L);
                    }
                default:
                    break;
            }
           return null;
        }

        /**
         * @param resultData
         * @param cacheProperty
         * @return
         */
        private static Object  getResultData(String resultData,RedisCacheProperty cacheProperty){
            if(resultData != null){
                if(resultData.startsWith("[")) {
                    return JSON.parseArray(resultData, cacheProperty.getTargetClass());
                }else if(resultData.startsWith("{")){
                    return JSON.parseObject(resultData, cacheProperty.getTargetClass());
                }else{
                    return resultData;
                }
            }
            return null;
        }
    }

    /**
     * 生成缓存数据Key
     * @param cacheProperty
     * @return
     */
    private  static String generateShardCacheKey(RedisCacheProperty cacheProperty){
        String cacheKey = "";
        //自动分片
        if(cacheProperty.isAutoShard()){
            cacheKey = generateShardKey(cacheProperty);
        }else{
            cacheKey = cacheProperty.getCacheKey();
        }
        return cacheKey;
    }

    /**
     * 生成分片的Key
     *
     * @param redisCacheProperty
     * @return
     */
    private static String generateShardKey(RedisCacheProperty redisCacheProperty) {
        String shardKey = redisCacheProperty.getShardKey();
        String keySuffix;
        if (!StringUtils.isEmpty(shardKey)) {
            //根据业务规则属性实现分片策略
            //int keyOffset = Math.abs(Objects.hashCode(shardKey)) % VIRTUAL_NODE_NUMBER;
            keySuffix = redisCacheProperty.getShardKeyName()+KEY_SEPERATOR+shardKey;
        }else{
            //默认分片策略
            int keyOffset =  Math.abs(Objects.hashCode(redisCacheProperty.getId())) % redisCacheProperty.getShardNum();
            keySuffix =  "shard"+keyOffset;
        }
        return redisCacheProperty.getCacheKey() + KEY_SEPERATOR + keySuffix;
    }

}
