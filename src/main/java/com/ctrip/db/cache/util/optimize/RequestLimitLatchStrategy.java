package com.ctrip.db.cache.util.optimize;


import com.ctrip.db.cache.annotation.RedisCacheProperty;
import com.ctrip.db.cache.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 请求限流控制器
 */
public class RequestLimitLatchStrategy {

    private  static final Logger LOGGER = LoggerFactory.getLogger(RequestLimitLatchStrategy.class);
    /**
     * 检查当前数是否超过指定的请求阈值
     * @param redisCacheProperty
     * @return
     */
    public static boolean checkCurrentRequestThreshold(RedisCacheProperty redisCacheProperty){
        //最大请求数
        int maxRequests = redisCacheProperty.getMaxRequests();
        //时间窗口
        int timeWindow = redisCacheProperty.getTimeWindow();
        String operateName = redisCacheProperty.getOperateName();
        String limitCacheKey = redisCacheProperty.isInConditionClause() ? operateName+":limit_in" : operateName+":limit_e";
        Long currentRequest = RedisUtil.incrBy(limitCacheKey, 1);
        //第一次访问
        if(currentRequest == 1){
            RedisUtil.expire(limitCacheKey,timeWindow);
        }
        if(currentRequest > maxRequests){
            System.out.println("被限流降级了...");
            LOGGER.warn("[[title=RequestLimitLatch]] 当前的请求threadId:{}被限流降级了：{}={}",Thread.currentThread().getId(),redisCacheProperty.getKeyName(),redisCacheProperty.getId());
            //限流降级了
            return false;
        }
        return true;
    }
    
}
