# Redis Cache Synchronous Framework

# 一.基于MyBatis+springboot+redis实现的缓存同步
### 底层基于Mybatis拦截器机制实现缓存同步操作，满足大部分业务场景需求，所有的缓存接口配置都在redis中进行配置， 从而实现缓存配置驱动开发。
## Framework Fetures：
- 采用非侵入式，微内核+插件体系结构开发，基于mybatis拦截器实现DAL层系统流量拦截
- 采用配置驱动开发，所有的缓存接口都在redis中key为hash结构的dlt_redis_cache_config中进行配置，来实现DB和redis缓存数据同步，开发人员不用关心底层的缓存实现，提供透明化的使用方式
- 避免缓存接口频繁对redis的访问，加入本地缓存策略。采用redis发布/订阅机制实现新增、更新、删除缓存操作实时同步到本地缓存。
- 为了提高应用的读写性能，采用异步缓存同步策略，支持查询，更新，插入，批量插入，删除操作的缓存同步。
- 支持mysql和sqlserver等多种数据库以及存储过程调用缓存同步实现。
- 为了保证数据的有效性，支持缓存部分存储字段。
- 支持类似sql查询方式：=、!=、>、>=、<、<=、like、is null、is not null、in。
- 支持数据分片，保证多个集群节点数据均衡分布和请求负载均衡。
- 支持采用gzip压缩算法对大数据的压缩存储。
- 支持缓存治理优化策略，采用二级缓存和限流降级/限速方式防止缓存穿透和缓存雪崩。
- 为了便于缓存的管理和维护，提供多种http endpoints,实现端点智能化运维管理方式。
- 提供零侵入、零开发、零发布方式，配置立即生效方式实现缓存同步。

# 二.开发指南

### 1.在应用的pom.xml文件中增加以下依赖:
####        &lt;dependency&gt;
####      &nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.ctrip.hotel.db.cache&lt;/groupId&gt;
####      &nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;redis-cache-sync-starter&lt;/artifactId&gt;
####      &nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;1.0.2&lt;/version&gt;
####        &lt;/dependency&gt;

### 2.在应用的配置文件application.properties或者在dataaccess模块的classpath加入cache-sync.properties文件中统一应用加入:
#### db.cache.redis.cluster-name=HotelDltCacheNew(redis集群的名称)（必须指定）
#### db.cache.redis.config-key-name=（指定缓存配置Key，默认为dlt_redis_cache_config，可选）


# 三.文档地址：http://conf.ctripcorp.com/pages/viewpage.action?pageId=145929670