package com.ctrip.db.cache.redis;


import com.ctrip.db.cache.annotation.RedisCache;
import com.ctrip.db.cache.annotation.RedisCacheProperty;
import com.ctrip.db.cache.util.DefaultCacheOptions;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * Redis 缓存处理器，用于检查注解配置和文件配置方式
 * Created by zhao.yong on 2017/9/28.
 */
public class RedisCacheHandler {

    /**
     * 检查是否包含@RedisCache{@link RedisCache }注解
     * @param sqlId
     * @param paramObject
     * @return
     * @throws Exception
     */
    @Deprecated
    public List<RedisCacheProperty> checkRedisCacheAnnoation(BoundSql boundSql, String sqlId, Object paramObject) throws Exception {
        int index = sqlId.lastIndexOf(".");
        String interfaceClass = sqlId.substring(0, index);
        //目标接口类
        Class<?> clazz = Class.forName(interfaceClass);
        //目标接口方法名
        String methodName = sqlId.substring(index + 1);
        //目标接口对应的方法
        Method method = null;
        //参数类型判断
        if(paramObject == null ){
            method = clazz.getDeclaredMethod(methodName);
        }else if (paramObject instanceof Map) {
            Map<String, Object> paramMap = (Map) paramObject;
            if (paramMap != null && paramMap.size() > 0) {
                Class<?> paramClazz = null;
                if (paramMap.containsKey("param1")) {
                    Object value = paramMap.get("param1");
                    paramClazz = value.getClass();
                }
                //如果是集合类
                else if(paramMap.containsKey("collection")) {
                    paramClazz = List.class;
                }else{
                    if(paramObject.getClass().equals(HashMap.class)){
                        paramClazz = HashMap.class;
                    }else{
                        paramClazz = Map.class;
                    }
                }

                method = clazz.getDeclaredMethod(methodName, paramClazz);

                //判断方法是否存在RedisCache注解
                if (method.isAnnotationPresent(RedisCache.class)) {
                    RedisCache cache = method.getDeclaredAnnotation(RedisCache.class);
                    String key = cache.key();
                    Class<?> aClass = cache.cacheObject();
                    //过期时间设置
                    long expireTime = cache.expireTime();
                    String tableName = cache.tableName();
                    String cacheKey = StringUtils.isEmpty(tableName) ? getCacheKey(boundSql) : tableName.toLowerCase();
                    String[] excludeColumns = cache.excludeFields();
                    DefaultCacheOptions.StoreType storeType = cache.dataType();
                    //集合类型
                    if (paramClazz.isAssignableFrom(List.class)) {
                        if (aClass.isAssignableFrom(Object.class)) {
                            aClass = ResolvableType.forMethodParameter(method, 0).getGeneric(0).getRawClass();
                        }
                        Collection<Object> collection = (Collection<Object>) paramMap.get("collection");
                        List<RedisCacheProperty> cacheList = new ArrayList<>();
                        for (Object o : collection) {
                            Class<?> oClass = o.getClass();
                            Method method1 = oClass.getMethod("get" + StringUtils.capitalize(key));
                            cacheList.add(new RedisCacheProperty(method1.invoke(o),key, oClass,cacheKey ,expireTime,excludeColumns,storeType.name()));
                        }
                        return cacheList;
                    } else {
                        Object obj = paramMap.get(key);
                        return Arrays.asList(new RedisCacheProperty(obj, key,aClass, cacheKey,expireTime,excludeColumns,storeType.name()));
                    }

                }
            }
        } else {
            method = clazz.getDeclaredMethod(methodName, paramObject.getClass());
            if (method.isAnnotationPresent(RedisCache.class)) {
                RedisCache cache = method.getDeclaredAnnotation(RedisCache.class);
                Field keyField = paramObject.getClass().getDeclaredField(cache.key());
                keyField.setAccessible(true);
                long expireTime = cache.expireTime();
                Class<?> aClass = paramObject.getClass();
                String tableName = cache.tableName();
                String cacheKey = StringUtils.isEmpty(tableName) ? getCacheKey(boundSql) : tableName.toLowerCase();
                String[] excludeColumns = cache.excludeFields();
                DefaultCacheOptions.StoreType storeType = cache.dataType();
                return Arrays.asList(new RedisCacheProperty(keyField.get(paramObject),cache.key(), aClass, cacheKey,expireTime,excludeColumns,storeType.name()));
            }
        }

        return null;
    }

    /**
     * 检查是否包含缓存配置
     * @param sqlId
     * @param paramObject
     * @return
     * @throws Exception
     */
    public List<RedisCacheProperty> checkRedisCacheConfig(BoundSql boundSql, String sqlId, Object paramObject) throws Exception {
        if(CacheManager.CACHE_MAPPINGS.containsKey(sqlId)){
            String value = CacheManager.CACHE_MAPPINGS.get(sqlId);
            List<Condition> keyPropertyList = new ArrayList<>(4);
            Map<String, String> map = parameterizeMap(value);
            String key = map.get("key");
            //支持key的组合键输出
            String[] keys = key.split(",");

            String expireTimeStr = map.get("expireTime");
            long expireTime = StringUtils.hasText(expireTimeStr) ? Long.valueOf(expireTimeStr) : 0;
            String tableName = map.get("tableName");
            String cacheKey = StringUtils.isEmpty(tableName) ? getCacheKey(boundSql) : tableName.toLowerCase();
            String excludeColumns = map.get("excludeFields");
            String[] excludeColumnArray = null;
            if(!StringUtils.isEmpty(excludeColumns)){
                excludeColumnArray = excludeColumns.split(",");
            }
            String cacheObject = map.get("cacheObject");
            Class<?> aClass = null;
            if(!StringUtils.isEmpty(cacheObject)){
                //查询操作
                aClass  = Class.forName(cacheObject);
            }
            String dataType = map.get("dataType");
            //查询是否压缩
            String compressed = map.get("compressed");
            boolean isCompressed = StringUtils.isEmpty(compressed) ? false : Boolean.valueOf(compressed);
            //数据压缩类型
            String compressType = map.get("compressType");
            String autoShard = map.get("autoShard");
            boolean isAutoShard = StringUtils.isEmpty(autoShard) ? false : Boolean.valueOf(autoShard);
            String shardKey = map.get("shardKey");
            String configShardNum = map.get("shardNum");
            int shardNum = 0;
            if(!StringUtils.isEmpty(configShardNum)){
                shardNum = Integer.valueOf(configShardNum);
            }
            //缓存治理选项
            //最大请求数
            String maxRequests = map.get("maxRequests");
            //时间窗口
            String timeWindow = map.get("timeWindow");
            //限流策略
            String limitStrategy = map.get("limitStrategy");
            String limitCacheExpireTime = map.get("limitCacheExpireTime");
            if(paramObject instanceof MapperMethod.ParamMap){
                MapperMethod.ParamMap paramMap =  (MapperMethod.ParamMap)paramObject;
                //包含@Param注解
                if(paramMap.containsKey("param1") && !ClassUtils.isPrimitiveOrWrapper(paramMap.get("param1").getClass())){
                    //更新操作
                    Object targetObject = paramMap.get("param1");
                     aClass = aClass == null ? targetObject.getClass() : aClass;
                   List<String> keyList = new LinkedList<>() ;
                    for(String singleKey : keys){
                        Method method = aClass.getMethod("get" + StringUtils.capitalize(singleKey));
                        String propertyValue = String.valueOf( method.invoke(targetObject));
                        keyList.add(propertyValue);
                        keyPropertyList.add(new Condition(singleKey+" = ",propertyValue));
                    }

                    RedisCacheProperty redisCacheProperty = new RedisCacheProperty(StringUtils.collectionToDelimitedString(keyList, "_"), key, keyPropertyList, aClass, cacheKey, expireTime, excludeColumnArray, dataType, null, isCompressed, compressType);
                    redisCacheProperty.setAutoShard(isAutoShard);
                    redisCacheProperty.setShardKey(getShardKeyValue(aClass,targetObject,shardKey));
                    redisCacheProperty.setShardKeyName(shardKey);
                    if(shardNum > 0){
                        redisCacheProperty.setShardNum(shardNum);
                    }
                    setLimitLatchOption(redisCacheProperty,sqlId,maxRequests,timeWindow,limitStrategy,limitCacheExpireTime);
                    return Arrays.asList(redisCacheProperty);

                }else{
                    List<String> keyList = new LinkedList<>() ;
                    for(String singleKey : keys){
                        String propertyValue = String.valueOf(paramMap.get(singleKey));
                        keyPropertyList.add(new Condition(singleKey+" = ",propertyValue));
                        keyList.add(propertyValue);
                    }
                    RedisCacheProperty redisCacheProperty = new RedisCacheProperty(StringUtils.collectionToDelimitedString(keyList, "_"), key, keyPropertyList, aClass, cacheKey, expireTime, excludeColumnArray, dataType, null, isCompressed, compressType);
                    redisCacheProperty.setAutoShard(isAutoShard);
                    redisCacheProperty.setShardKey(getShardKeyValue(aClass,paramMap,shardKey));
                    redisCacheProperty.setShardKeyName(shardKey);
                    if(shardNum > 0){
                        redisCacheProperty.setShardNum(shardNum);
                    }
                    setLimitLatchOption(redisCacheProperty,sqlId,maxRequests,timeWindow,limitStrategy,limitCacheExpireTime);
                    return Arrays.asList(redisCacheProperty);
                }

            }else{
                Class<?> tempClass = paramObject.getClass();
                Class<?> targetClass = tempClass;
                if(!StringUtils.isEmpty(cacheObject)){
                    targetClass = Class.forName(cacheObject);
                }
                //判断条件字段是否存在(兼容条件查询方式)
                Field oredCriteria = null;
                try {
                     oredCriteria = tempClass.getDeclaredField("oredCriteria");
                }catch (Exception e){
                    return new ArrayList<>();
                }
                if(oredCriteria != null){
                    oredCriteria.setAccessible(true);
                   List list = (List) oredCriteria.get(paramObject);
                    List<String> keyList = new LinkedList<>();
                    List<String> shardKeyList = new LinkedList<>();
                    List<Condition> conditions = new LinkedList<>();
                    boolean inConditionClause = false;
                   stop : for(Object object : list){
                       List criterionList = (List) object.getClass().getDeclaredMethod("getCriteria").invoke(object);
                       for(Object criterion : criterionList){
                           Field condition = criterion.getClass().getDeclaredField("condition");
                           condition.setAccessible(true);
                           String condi = condition.get(criterion).toString().toLowerCase();
                           Field valueField = criterion.getClass().getDeclaredField("value");
                           valueField.setAccessible(true);
                           //处理等于
                           boolean present = Stream.of(key.split(",")).filter(tempKey -> condi.equals(tempKey + " =")).findFirst().isPresent();
                           //shardKey分片
                           if(!StringUtils.isEmpty(shardKey)){
                               boolean shardKeyPersent = Stream.of(shardKey.split(",")).filter(tempKey -> condi.equals(tempKey + " =")).findFirst().isPresent();
                               if(shardKeyPersent){
                                   String propetyValue = String.valueOf(valueField.get(criterion));
                                   shardKeyList.add(propetyValue);
                               }

                           }
                           //处理in语句
                           boolean inPresent = Stream.of(key.split(",")).filter(tempKey -> condi.equals(tempKey + " in")).findFirst().isPresent();
                           if(present){
                               String propetyValue = String.valueOf(valueField.get(criterion));
                               keyList.add(propetyValue);
                               keyPropertyList.add(new Condition(condi,propetyValue));
                           }
                           else if(inPresent){
                               Object paramObj = valueField.get(criterion);
                               String propetyValue = StringUtils.collectionToDelimitedString((List) paramObj, ",");
                               keyList.add(propetyValue);
                               keyPropertyList.add(new Condition(condi,propetyValue));
                               inConditionClause = true;
                           }
                           else{
                               conditions.add(new Condition(condi,valueField.get(criterion)));
                           }
                       }
                   }
                   if(keyList.size() > 0){
                       RedisCacheProperty redisCacheProperty = new RedisCacheProperty(StringUtils.collectionToDelimitedString(keyList, "_"), key, keyPropertyList, targetClass, cacheKey, expireTime, excludeColumnArray, dataType, conditions, inConditionClause, isCompressed, compressType);
                       redisCacheProperty.setAutoShard(isAutoShard);
                       if(!CollectionUtils.isEmpty(shardKeyList)){
                           redisCacheProperty.setShardKey(shardKeyList.get(0));
                           redisCacheProperty.setShardKeyName(shardKey);
                       }
                       if(shardNum > 0){
                           redisCacheProperty.setShardNum(shardNum);
                       }
                       setLimitLatchOption(redisCacheProperty,sqlId,maxRequests,timeWindow,limitStrategy,limitCacheExpireTime);
                       return Arrays.asList(redisCacheProperty);
                   }
                } else {
                    List<String> keyList = new LinkedList<>() ;
                    for(String singleKey : keys){
                        Method method = tempClass.getMethod("get" + StringUtils.capitalize(singleKey));
                        String propertyValue = String.valueOf(method.invoke(paramObject));
                        keyList.add(propertyValue);
                        keyPropertyList.add(new Condition(singleKey+" = ",propertyValue));
                    }
                    RedisCacheProperty redisCacheProperty = new RedisCacheProperty(StringUtils.collectionToDelimitedString(keyList, "_"), key, keyPropertyList, targetClass, cacheKey, expireTime, excludeColumnArray, dataType, null, isCompressed, compressType);
                    redisCacheProperty.setAutoShard(isAutoShard);
                    redisCacheProperty.setShardKey(getShardKeyValue(tempClass,paramObject,shardKey));
                    redisCacheProperty.setShardKeyName(shardKey);
                    if(shardNum > 0){
                        redisCacheProperty.setShardNum(shardNum);
                    }
                    setLimitLatchOption(redisCacheProperty,sqlId,maxRequests,timeWindow,limitStrategy,limitCacheExpireTime);
                    return Arrays.asList(redisCacheProperty);
                }
            }

        }
        return null;
    }


    /**
     * 设置限流操作选项
     * @param redisCacheProperty
     * @param maxRequests
     * @param timeWindow
     * @param limitStrategy
     * @param limitCacheExpireTime
     */
    private void setLimitLatchOption(RedisCacheProperty redisCacheProperty,String sqlId,String maxRequests,
                                     String timeWindow,String limitStrategy,String limitCacheExpireTime){

        redisCacheProperty.setOperateName(sqlId);
        if(!StringUtils.isEmpty(maxRequests)){
            redisCacheProperty.setMaxRequests(Integer.valueOf(maxRequests));
        }
        if(!StringUtils.isEmpty(timeWindow)){
            redisCacheProperty.setTimeWindow(Integer.valueOf(timeWindow));
        }
        if(!StringUtils.isEmpty(limitStrategy)){
            redisCacheProperty.setLimitStrategy(limitStrategy);
        }
        if(!StringUtils.isEmpty(limitCacheExpireTime)){
            redisCacheProperty.setLimitCacheExpire(Long.valueOf(limitCacheExpireTime));
        }
    }
    /**
     * 设置分片key的数据值
     * @param clazz
     * @param targetObject
     * @param shardKey
     * @return
     * @throws Exception
     */
    private String getShardKeyValue(Class<?> clazz,Object targetObject,String shardKey) throws Exception {
        if(!StringUtils.isEmpty(shardKey)){
            if(targetObject instanceof Map){
                Map map = (Map)targetObject;
                return String.valueOf(map.get(shardKey));
            }else{
                Method method = clazz.getMethod("get" + StringUtils.capitalize(shardKey));
                return String.valueOf(method.invoke(targetObject));
            }

        }
        return null;
    }

    /**
     * 通过SQL语句获取缓存的KEY
     * @param boundSql
     * @return
     */
    private String getCacheKey(BoundSql boundSql){
        String sql = boundSql.getSql().toLowerCase();
        if(sql.contains("insert")){
            int start = "insert into".length()+1;
            int end = sql.indexOf("(");
            return sql.substring(start,end).trim();
        }else if(sql.contains("update")){
            int start = sql.indexOf("update")+6;
            int end = sql.indexOf("set");
            return sql.substring(start,end).trim();
        }else if(sql.contains("select")){
            int start = sql.indexOf("from")+4;
            int end = sql.indexOf("where") > 0 ? sql.indexOf("where") : (sql.indexOf("limit") > 0 ? sql.indexOf("limit") : sql.length());
            String result = sql.substring(start, end);
            int n = result.indexOf("(");
            if( n > 0){
                result =   result.substring(0,n);
            }
            return result.trim();
        }else if(sql.contains("delete")){
            int start = sql.indexOf("delete from")+11;
            int end = sql.indexOf("where");
            return sql.substring(start,end).trim();
        }
        return "";
    }


    /**
     * 缓存参数化的Map
     * @param paramValue
     * @return
     */
    private Map<String,String> parameterizeMap(String paramValue){
        Map<String,String> paramMap = new HashMap();
        String[] results = paramValue.split(";");
        if(results!= null && results.length > 0){
            for(String result : results) {
                String[] split = result.split("=");
                paramMap.put(split[0], split[1]);
            }
        }
        return paramMap;
    }
}
