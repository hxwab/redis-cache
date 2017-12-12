package com.ctrip.db.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerUIConfiguration {

    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ctrip.db.cache.http"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo buildApiInfo(){
        return new ApiInfoBuilder()
                .title("Redis Synchronouse Framework缓存管理文档")
                .description("基于Redis缓存同步框架，源码链接：http://git.dev.sh.ctripcorp.com/hotel-dlt/redis-cache-sync-starter")
                .termsOfServiceUrl("http://conf.ctripcorp.com/pages/viewpage.action?pageId=145929670")
                .version("1.0")
                .contact(new Contact("zhao.yong","http://conf.ctripcorp.com/pages/viewpage.action?pageId=145929670","zhao.yong@ctrip.com"))
                .build();
    }
}
