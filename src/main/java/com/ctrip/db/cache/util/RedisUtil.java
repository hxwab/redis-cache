package com.ctrip.db.cache.util;

import com.alibaba.fastjson.JSON;
import credis.java.client.CacheProvider;
import credis.java.client.RedisPubSub;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RedisUtil {

    private static CacheProvider cacheProvider;
    public static void setCacheProvider(CacheProvider cacheProvider){
        RedisUtil.cacheProvider = cacheProvider;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> tClass)  {
        try {
            String value = cacheProvider.get(key);
            if (value == null) {
                return null;
            }
            return tClass == String.class ? (T) value : JSON.parseObject(value, tClass);
        } catch (Exception ex) {
            throw new RuntimeException("RedisUtilNew.get 异常", ex);
        }
    }

    public static byte[] get(byte[] key)  {
        try {
            return cacheProvider.get(key);
        } catch (Exception ex) {
            throw new RuntimeException("RedisUtilNew.get 异常", ex);
        }
    }

    public static <T> boolean set(String key, T value, Class<T> tClass)  {
        try {
            String json = tClass == String.class ? (String) value : JSON.toJSONString(value);
            return cacheProvider.set(key, json);
        } catch (Exception ex) {
            throw new RuntimeException("RedisUtilNew.set 异常", ex);
        }
    }
    public static boolean set(byte[] key, byte[] value)  {
        try {
            return cacheProvider.set(key, value);
        } catch (Exception ex) {
            throw new RuntimeException("RedisUtilNew.set 异常", ex);
        }
    }

    public static Long incrBy(String key, long increment)  {
        return cacheProvider.incrBy(key, increment);
    }

    public static String mset(HashMap<String,byte[]> dataMap)  {
        try {
            return cacheProvider.mset(dataMap);
        } catch (Exception ex) {
            throw new RuntimeException("RedisUtilNew.mset 异常", ex);
        }
    }

    public static String mset(String... datas)  {
        try {
            return cacheProvider.mset(datas);
        } catch (Exception ex) {
            throw new RuntimeException("RedisUtilNew.mset 异常", ex);
        }
    }

    public static <T> boolean set(String key, T value, Class<T> tClass, int ttl)  {
        try {
            String json = tClass == String.class ? (String) value : JSON.toJSONString(value);
            return cacheProvider.setex(key, ttl, json);
        } catch (Exception ex) {
            throw new RuntimeException("RedisUtilNew.set 异常", ex);
        }
    }

    public static boolean del(String key)  {
        try {
            return cacheProvider.del(key);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.del 异常", e);
        }
    }

    public static long del(String... keys){
        try {
            return cacheProvider.del(keys);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.del 删除多个异常", e);
        }
    }

    public static Set<String> keys(String pattern){
        try {
            return cacheProvider.keys(pattern);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.keys 异常", e);
        }
    }
    /**
     * 是否存在Key
     *
     * @param key
     * @return
     */
    public static boolean existsKey(String key) {
        boolean result = false;
        if (key == null || key.length() <= 0) {
            return result;
        }
        try {
            return cacheProvider.exists(key);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.existsKey 异常", e);
        }
    }

    /**
     * 是否存在HashKey
     *
     * @param key
     * @return
     */
    public static boolean hExistsKey(String key, String hkey) {
        boolean result = false;
        if (key == null || key.length() <= 0 || hkey == null || hkey.length() <= 0) {
            return result;
        }
        try {
            result = cacheProvider.hexists(key, hkey);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.hExistsKey 异常", e);
        }
        return result;
    }

    /**
     * 获取HashKey
     *
     * @param key
     * @param hkey
     * @return
     */
    public static String hGet(String key, String hkey) {
        String result = "";
        if (key == null || key.length() <= 0 || hkey == null || hkey.length() <= 0) {
            return result;
        }
        try {
            result = cacheProvider.hget(key, hkey);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.hGet 异常", e);
        }
        return result;
    }

    public static byte[] hGet(byte[] key, byte[] hkey) {
        try {
            return cacheProvider.hget(key, hkey);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.hGet 异常", e);
        }
    }
    /**
     * 获取所有field-value
     *
     * @param key
     * @return
     */
    public static Map<String,String> hGetAll(String key) {
        Map<String,String> result = new HashMap<String,String>();
        if (key == null || key.length() <= 0) {
            return result;
        }
        try {
            result = cacheProvider.hgetAll(key);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.hGetAll 异常", e);
        }
        return result;
    }

    public static boolean hDel(String key,String... fields)  {
        try {
            return cacheProvider.hdel(key,fields)>0;
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.del 异常", e);
        }
    }

    public static long rPush(String key,String... value) {
        try {
            return cacheProvider.rpush(key, value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.rPush 异常", e);
        }
    }

    public static long rPush(byte[] key,byte[]... value) {
        try {
            return cacheProvider.rpush(key,value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.rPush 异常", e);
        }
    }

    public static long lPush(String key,String... value) {
        try {
            return cacheProvider.lpush(key, value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.lPush 异常", e);
        }
    }
    public static List<String> lRange(String key, Long start, Long end) {
        try {
            return cacheProvider.lrange(key,start,end);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.lRange 异常", e);
        }
    }

    public static List<byte[]> lRange(byte[] key, Long start, Long end) {
        try {
            return cacheProvider.lrange(key,start,end);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.lRange 异常", e);
        }
    }

    public static boolean lSet(String key,long index,String value) {
        try {
            return cacheProvider.lset(key, index,value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.lPush 异常", e);
        }
    }

    public static long sadd(String key,String... value) {
        try {
            return cacheProvider.sadd(key,value);
        }catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.sadd 异常", e);
        }
    }
    public static long sadd(byte[] key,byte[]... value) {
        try {
            return cacheProvider.sadd(key,value);
        }catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.sadd 异常", e);
        }
    }

    public static Set<String> smembers(String key) {
        try {
            return cacheProvider.smembers(key);
        }catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.smembers 异常", e);
        }
    }

    public static Set<byte[]> smembers(byte[] key) {
        try {
            return cacheProvider.smembers(key);
        }catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.smembers 异常", e);
        }
    }

    public static Set<String> zrange(String key,Long start,Long end) {
        try {
            return cacheProvider.zrange(key,start,end);
        }catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.zrange 异常", e);
        }
    }
    public static Set<byte[]> zrange(byte[] key,Long start,Long end) {
        try {
            return cacheProvider.zrange(key,start,end);
        }catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.zrange 异常", e);
        }
    }

    public static boolean zadd(String key,double score,String value) {
        try {
            return cacheProvider.zadd(key,score,value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.zadd 异常", e);
        }
    }
    public static boolean zadd(byte[] key,double score,byte[] value) {
        try {
            return cacheProvider.zadd(key,score,value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.zadd 异常", e);
        }
    }
    public static Set<Tuple> zrange(String key, long start, long end) {
        try {
            return cacheProvider.zrangeWithScores(key,start,end);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.zrange 异常", e);
        }
    }


    /**
     * 设置HashKey
     *
     * @param key
     * @param hkey
     * @param value
     * @return
     */
    public static boolean hSet(String key, String hkey, String value)  {
        if (StringUtils.isEmpty(key) ||StringUtils.isEmpty(hkey)) {
            return false;
        }
        try {
            cacheProvider.hset(key, hkey, value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.hSet 异常", e);
        }
        return true;
    }

    /**
     * 二进制存储
     * @param key
     * @param hkey
     * @param value
     * @return
     * @
     */
    public static boolean hSet(byte[] key, byte[] hkey, byte[] value)  {
        try {
            cacheProvider.hset(key,hkey,value);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.hSet 异常", e);
        }
        return true;
    }

    public  static boolean hmset(String key,Map<String,String> values) {
        if (StringUtils.isEmpty(key) || values == null) {
            return false;
        }
        try {
            cacheProvider.hmset(key, values);
        } catch (Exception e) {
            throw new RuntimeException("RedisUtilNew.hmset 异常", e);
        }
        return true;
    }
    /**
     * 设置key 的缓存时间，单位：second
     *
     * @param key
     * @param ttl
     * @return
     */
    public static boolean expire(String key, int ttl) {
        if (existsKey(key) && ttl >= 0) {
            try {
                return cacheProvider.expire(key, ttl);
            } catch (Exception e) {
                throw new RuntimeException("RedisUtilNew.expire 异常", e);
            }
        }
        return false;
    }

    /**
     * 发送消息
     * @param channel  消息频道
     * @param message  发送的消息
     */
    public static void publish(String channel,String message){
        cacheProvider.publish(channel,message);
    }

    /**
     * 订阅消息
     * @param  messageListener 消息监听器
     * @param channels  消息频道
     */
    public static void subscribe(MessageListener messageListener,String... channels){
        cacheProvider.subscribe(messageListener,channels);
    }

    /**
     * 消息监听器
     */
    public static  abstract class MessageListener extends RedisPubSub {};
}
