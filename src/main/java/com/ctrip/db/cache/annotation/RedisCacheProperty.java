package com.ctrip.db.cache.annotation;


import com.ctrip.db.cache.redis.Condition;
import com.ctrip.db.cache.util.DefaultCacheOptions;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 缓存属性对象
 * Created by zhao.yong on 2017/9/28.
 */
public class RedisCacheProperty {
/**
 * ===============================基本配置选项===========================
 */
    /**
     * hashkey,一般是实体类的主键
     */
    private Object id;
    /**
     * 缓存操作接口名
     */
    private String operateName;
    /**
     * key 的名称
     */
    private String keyName;
    /**
     * key属性列表
     */
    private List<Condition> keyPropertyList;
    /**
     * 实体类对象
     */
    private Class<?> targetClass;
    /**
     * 缓存的KEY，一般是实体类的全类名
     */
    private String cacheKey;

    /**
     * 过期时间
     */
    private long expireTime;

    /**
     * 排除缓存的字段
     */
    private String[] excludeFields;

    /**
     * 指定redis存储的数据类型,默认采用hash存储
     */
    private String dataType = DefaultCacheOptions.StoreType.HASH.name().toLowerCase();

    private List<Condition> conditionList;

    /**
     * 是否包含in 操作语句
     */
    private boolean inConditionClause;

    /**
     * ===============================数据压缩选项===========================
     */
    /**
     * 是否压缩进行数据压缩，默认不采用压缩
     */
    private boolean compressed;

    /**
     *  压缩类型，如果采用压缩，默认采用Zip进行数据压缩
     */
    private String compressType;
/**
 * ===============================数据分片选项===========================
 */
    /**
     * 是否自动分片，默认为不开启自动分片
     */
    private boolean autoShard;

    /**
     * 分片数量,默认100个分片
     */
    private int shardNum = DefaultCacheOptions.VIRTUAL_NODE_NUMBER;

    /**
     * 分片Key
     */
    private String shardKey;
    /**
     * 分片属性的名称
     */
    private String shardKeyName;
    /**
     * ===============================缓存性能治理选项===========================
     */
    /**
     * 默认的治理策略为二级缓存策略
     */
    private String limitStrategy = DefaultCacheOptions.LimitStrategy.BASED_SECOND_CACHE.toString();

    /**
     * 限流缓存过期时间
     */
    private long limitCacheExpire=DefaultCacheOptions.LIMIT_CACHE_EXPIRE;
    /**
     * 最大请求数,默认为1000
     */
    private int maxRequests = DefaultCacheOptions.MAX_REQUESTS;
    /**
     * 滑动时间窗口，默认为1分钟
     */
    private int timeWindow = DefaultCacheOptions.TIME_WINDOW;

    public RedisCacheProperty() {
    }

    public RedisCacheProperty(Object id,String keyName,Class<?> targetClass, String cacheKey) {
        this.id = id;
        this.cacheKey = cacheKey;
        this.targetClass = targetClass;
        this.keyName = keyName;
    }

    public RedisCacheProperty(Object id,String keyName, Class<?> targetClass, String cacheKey, long expireTime) {
        this(id,keyName,targetClass,cacheKey);
        this.expireTime = expireTime;
    }

    public RedisCacheProperty(Object id,String keyName, Class<?> targetClass, String cacheKey, long expireTime, String[] excludeFields,String dataType) {
       this(id,keyName,targetClass,cacheKey,expireTime,excludeFields,dataType,null);
    }


    public RedisCacheProperty(Object id,String keyName, Class<?> targetClass, String cacheKey, long expireTime, String[] excludeFields, String dataType, List<Condition> conditionList) {
        this.id = id;
        this.keyName = keyName;
        this.targetClass = targetClass;
        this.cacheKey = cacheKey;
        this.expireTime = expireTime;
        this.excludeFields = excludeFields;
        if(StringUtils.hasText(dataType)){
            this.dataType = dataType;
        }
        this.conditionList = conditionList;
    }

    public RedisCacheProperty(Object id, String keyName,List<Condition> keyPropertyList, Class<?> targetClass, String cacheKey, long expireTime, String[] excludeFields, String dataType, List<Condition> conditionList,boolean compressed,String compressType) {
        this.id = id;
        this.keyName = keyName;
        this.keyPropertyList = keyPropertyList;
        this.targetClass = targetClass;
        this.cacheKey = cacheKey;
        this.expireTime = expireTime;
        this.excludeFields = excludeFields;
        if(StringUtils.hasText(dataType)){
            this.dataType = dataType;
        }
        this.conditionList = conditionList;
        this.compressed =  compressed;
        this.compressType = compressType;
    }

    public RedisCacheProperty(Object id,String keyName, List<Condition> keyPropertyList, Class<?> targetClass, String cacheKey, long expireTime, String[] excludeFields,
                              String dataType, List<Condition> conditionList, boolean inConditionClause,boolean compressed,String compressType) {
        this(id,keyName,keyPropertyList,targetClass,cacheKey,expireTime,excludeFields,dataType,conditionList,compressed,compressType);
        this.inConditionClause = inConditionClause;
    }

    public String getOperateName() {
        return operateName;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isInConditionClause() {
        return inConditionClause;
    }

    public void setInConditionClause(boolean inConditionClause) {
        this.inConditionClause = inConditionClause;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public List<Condition> getKeyPropertyList() {
        return keyPropertyList;
    }

    public void setKeyPropertyList(List<Condition> keyPropertyList) {
        this.keyPropertyList = keyPropertyList;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String[] getExcludeFields() {
        return excludeFields;
    }

    public void setExcludeFields(String[] excludeFields) {
        this.excludeFields = excludeFields;
    }

    public List<Condition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<Condition> conditionList) {
        this.conditionList = conditionList;
    }

    public boolean getCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressType(String compressType) {
        this.compressType = compressType;
    }

    public boolean isAutoShard() {
        return autoShard;
    }

    public void setAutoShard(boolean autoShard) {
        this.autoShard = autoShard;
    }

    public String getShardKey() {
        return shardKey;
    }

    public void setShardKey(String shardKey) {
        this.shardKey = shardKey;
    }

    public String getShardKeyName() {
        return shardKeyName;
    }

    public void setShardKeyName(String shardKeyName) {
        this.shardKeyName = shardKeyName;
    }

    public int getShardNum() {
        return shardNum;
    }

    public void setShardNum(int shardNum) {
        this.shardNum = shardNum;
    }

    public String getLimitStrategy() {
        return limitStrategy;
    }

    public void setLimitStrategy(String limitStrategy) {
        this.limitStrategy = limitStrategy;
    }

    public long getLimitCacheExpire() {
        return limitCacheExpire;
    }

    public void setLimitCacheExpire(long limitCacheExpire) {
        this.limitCacheExpire = limitCacheExpire;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }
}
