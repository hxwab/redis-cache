# Redis Cache Synchronous Framework

## 基于MyBatis+springboot+redis实现的缓存同步
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

## 文档地址：http://conf.ctripcorp.com/pages/viewpage.action?pageId=145929670