package com.ctrip.db.cache;

import com.ctrip.db.cache.http.CacheCommonController;
import com.ctrip.db.cache.http.HttpCacheManager;
import com.ctrip.db.cache.intercept.RedisCacheIntercepter;
import com.ctrip.db.cache.redis.RedisCacheHandler;
import com.ctrip.db.cache.redis.RedisOperateCommand;
import com.ctrip.db.cache.redis.RedisOperateProxy;
import com.ctrip.db.cache.util.CacheConfigProperties;
import com.ctrip.db.cache.util.RedisUtil;
import credis.java.client.CacheProvider;
import credis.java.client.util.CacheFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Configuration
@ImportAutoConfiguration(SwaggerUIConfiguration.class)
//@EnableConfigurationProperties(value = CacheConfigProperties.class)
@PropertySource("classpath:cache-sync.properties")
@ConditionalOnBean(value={SqlSessionFactoryBean.class})
public class CacheSyncAutoConfiguration implements InitializingBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheSyncAutoConfiguration.class);

    @Autowired
    private GenericApplicationContext applicationContext;

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

    @ConditionalOnProperty(prefix = "db.cache",name = "redis.cluster-name")
    @Bean
    public CacheConfigProperties getCacheConfigProperties() {
        return new CacheConfigProperties();
    }

    @Bean
    public RedisCacheHandler initRedisCacheHandler(){
        return new RedisCacheHandler();
    }

    @Bean
    public RedisOperateProxy initRedisOperateProxy(){
        return new RedisOperateProxy();
    }

    @ConditionalOnBean(CacheConfigProperties.class)
    @Bean
    public HttpCacheManager initHttpCacheManager(){
        return new HttpCacheManager();
    }


    @ConditionalOnBean(CacheConfigProperties.class)
    @Bean
    public CacheCommonController initCacheCommonController(){
        return new CacheCommonController();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("缓存同步框架自动注册CacheSyncAutoConfiguration...");
    }
}
