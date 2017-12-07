package com.ctrip.db.cache;

import com.ctrip.db.cache.http.CacheCommonController;
import com.ctrip.db.cache.http.HttpCacheManager;
import com.ctrip.db.cache.intercept.RedisCacheIntercepter;
import com.ctrip.db.cache.redis.RedisCacheHandler;
import com.ctrip.db.cache.redis.RedisOperateProxy;
import com.ctrip.db.cache.util.CacheConfigProperties;
import com.ctrip.db.cache.util.RedisUtil;
import credis.java.client.CacheProvider;
import credis.java.client.util.CacheFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(value = CacheConfigProperties.class)
@ConditionalOnProperty(prefix = "db.cache",name = "redis.cluster-name")
@ConditionalOnBean(value={SqlSessionFactoryBean.class})
public class CacheSyncAutoConfiguration implements InitializingBean{

    @Autowired
    private CacheConfigProperties cacheConfigProperties;

    @Autowired
    private Environment environment;

    @Autowired
    private GenericApplicationContext applicationContext;
    @Bean
    public CacheConfiguration initCacheConfigBean(){
        return new CacheConfiguration();
    }

    @Bean
    public RedisCacheIntercepter initRedisCacheIntercepter() throws Exception {
        RedisCacheIntercepter redisCacheIntercepter = new RedisCacheIntercepter();
        SqlSessionFactoryBean sqlSessionFactoryBean = null;
        org.apache.ibatis.session.Configuration configuration = null;
        String[] beanNames = applicationContext.getBeanNamesForType(SqlSessionFactoryBean.class);
        for(String beanName : beanNames){
            sqlSessionFactoryBean = applicationContext.getBean(beanName,SqlSessionFactoryBean.class);
            configuration = sqlSessionFactoryBean.getObject().getConfiguration();
            configuration.addInterceptor(redisCacheIntercepter);
        }
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

    @Override
    public void afterPropertiesSet() throws Exception {
        String redisClusterName = cacheConfigProperties.getClusterName();
        CacheProvider cacheProvider = CacheFactory.GetProvider(redisClusterName);
        RedisUtil.setCacheProvider(cacheProvider);
        String configKey = cacheConfigProperties.getConfigKeyName();
        if(!StringUtils.isEmpty(configKey)){
            CacheConfiguration.CACHE_CONFIG_KEY = configKey;
        }

    }
}
