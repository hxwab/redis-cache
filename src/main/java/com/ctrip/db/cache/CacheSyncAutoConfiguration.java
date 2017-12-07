package com.ctrip.db.cache;

import com.ctrip.db.cache.aop.MyBatisInterceptorAspect;
import com.ctrip.db.cache.http.CacheCommonController;
import com.ctrip.db.cache.http.HttpCacheManager;
import com.ctrip.db.cache.intercept.RedisCacheIntercepter;
import com.ctrip.db.cache.redis.RedisCacheHandler;
import com.ctrip.db.cache.redis.RedisOperateProxy;
import com.ctrip.db.cache.util.RedisUtil;
import credis.java.client.CacheProvider;
import credis.java.client.util.CacheFactory;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(prefix = "db.cache",name = "redis.cluster.name")
@EnableAspectJAutoProxy
public class CacheSyncAutoConfiguration implements InitializingBean{

    @Autowired
    private Environment environment;

    @Autowired
    private GenericApplicationContext applicationContext;
    @Bean
    public CacheConfiguration initCacheConfigBean(){
        return new CacheConfiguration();
    }

    @Bean("redisCacheIntercepter")
    public RedisCacheIntercepter initRedisCacheIntercepter(){
        SqlSessionFactoryBean sqlSessionFactoryBean = applicationContext.getBean(SqlSessionFactoryBean.class);
        RedisCacheIntercepter redisCacheIntercepter = new RedisCacheIntercepter();
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{redisCacheIntercepter});
        return redisCacheIntercepter;
    }

    @Bean
    public RedisCacheHandler initRedisCacheHandler(){
        return new RedisCacheHandler();
    }

    @Bean
    public RedisOperateProxy initRedisOperateProxy(){
        return new RedisOperateProxy();
    }

    @Bean
    public HttpCacheManager initHttpCacheManager(){
        return new HttpCacheManager();
    }

    @Bean
    public CacheCommonController initCacheCommonController(){
        return new CacheCommonController();
    }
    @Bean
    public MyBatisInterceptorAspect initMyBatisInterceptorAspect(){
        return new MyBatisInterceptorAspect();
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        String redisClusterName = environment.getProperty("db.cache.redis.cluster.name");
        CacheProvider cacheProvider = CacheFactory.GetProvider(redisClusterName);
        RedisUtil.setCacheProvider(cacheProvider);
    }
}
