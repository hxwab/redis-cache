package com.ctrip.db.cache.aop;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import javax.annotation.PostConstruct;

@Aspect
public class MyBatisInterceptorAspect {

    @PostConstruct
    public void init(){
    }

    @Before("execution(* org.mybatis.spring..*.afterPropertiesSet(..))")
    public void addInterceptorHandler(JoinPoint joinPoint){
    }
}
