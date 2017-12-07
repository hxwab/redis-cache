package com.ctrip.db.cache.intercept;


import com.ctrip.db.cache.annotation.RedisCacheProperty;
import com.ctrip.db.cache.redis.CacheManager;
import com.ctrip.db.cache.redis.Condition;
import com.ctrip.db.cache.redis.RedisCacheHandler;
import com.ctrip.db.cache.redis.RedisOperateProxy;
import com.ctrip.db.cache.util.DefaultCacheOptions;
import com.ctrip.db.cache.util.RedisUtil;
import com.ctrip.db.cache.util.optimize.RequestLimitLatchStrategy;
import com.ctrip.db.cache.util.optimize.SecondLevelCacheStrategy;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by zhao.yong on 2017/9/27.
 */

/**
 * StatementHandler,ResultSetHandler,Executor,ParameterHandler
 */

@Intercepts({
       @Signature(type = Executor.class,method = "query",
                args = {MappedStatement.class,Object.class,RowBounds.class,ResultHandler.class}),
       @Signature(type = Executor.class,method = "update",args = {MappedStatement.class,Object.class}),
        @Signature(type = StatementHandler.class,method = "update",args = {Statement.class})})
public class RedisCacheIntercepter implements Interceptor {
    private Logger LOGGER = LoggerFactory.getLogger(RedisCacheIntercepter.class);
    //存储当前的MapperStatement
    private static ThreadLocal<MappedStatement> MAPPER_STATEMENT_HOLDER = new ThreadLocal<MappedStatement>();

    @Autowired
    private RedisOperateProxy redisOperateCommand;

    @Autowired
    private RedisCacheHandler redisCacheHandler;


    private static final String CACHE_SWITCH_KEY="redis_cache_switch";


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object result;
        if(target instanceof StatementHandler){
            RoutingStatementHandler statementHandler = (RoutingStatementHandler)target;
            BoundSql boundSql = statementHandler.getBoundSql();
            Object parameterObject = statementHandler.getParameterHandler().getParameterObject();
            MappedStatement mappedStatement = MAPPER_STATEMENT_HOLDER.get();
            String sqlId = mappedStatement.getId();
            //如果缓存不存在或者缓存开关关闭
            if(!CacheManager.CACHE_MAPPINGS.containsKey(sqlId) || !checkRedisSwitch()){
                return invocation.proceed();
            }
            result = invocation.proceed();
            //从配置文件读取
            List<RedisCacheProperty> cachePropertyList = redisCacheHandler.checkRedisCacheConfig(boundSql,sqlId, parameterObject);
           /* if(CollectionUtils.isEmpty(cachePropertyList)){
                //注解读取
                cachePropertyList = redisCacheHandler.checkRedisCacheAnnoation(boundSql,sqlId, parameterObject);
            }*/
            if(!CollectionUtils.isEmpty(cachePropertyList)){
                //保存Redis数据
                syncRedisCacheData(cachePropertyList,parameterObject,mappedStatement);
            }
            MAPPER_STATEMENT_HOLDER.remove();
        }else{
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            //如果是查询语句
            if(SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())){
                Object paramObj = invocation.getArgs()[1];
                String sqlId = mappedStatement.getId();
                if(!CacheManager.CACHE_MAPPINGS.containsKey(sqlId) || !checkRedisSwitch()){
                    return invocation.proceed();
                }

                BoundSql boundSql = mappedStatement.getBoundSql(paramObj);
                //从配置文件读取
                List<RedisCacheProperty> cachePropertyList = redisCacheHandler.checkRedisCacheConfig(boundSql,sqlId, paramObj);
                RedisCacheProperty cacheProperty = null;
                if(CollectionUtils.isEmpty(cachePropertyList)){
                    //注解处理
                    //cachePropertyList = redisCacheHandler.checkRedisCacheAnnoation(boundSql,sqlId, paramObj);
                }else{
                    cacheProperty = cachePropertyList.get(0);
                }
                Object cacheData;
                //没有缓存属性对象，则从DB查询
                if(cacheProperty ==  null){
                    cacheData = invocation.proceed();
                }else{
                    //其他的DB查询条件
                    List<Condition> conditionList = new LinkedList<>();
                    //从redis获取对象
                     cacheData = queryRedisCacheData(cacheProperty,conditionList);
                    //是否为[] List
                    boolean checkFlag = cacheData != null && (cacheData instanceof List) && CollectionUtils.isEmpty((List) cacheData);

                    //如果不存在缓存，加入缓存
                    if(cacheData == null || (checkFlag && cacheProperty.getExpireTime()==0)){
                        //判断过滤条件是否为空
                        cacheData = saveRedisData(mappedStatement,boundSql,invocation,cacheProperty);
                        cacheData = filterResultRecord(cacheData,cacheProperty);
                    }else{
                        //如果包含DB查询
                        if(!CollectionUtils.isEmpty(conditionList)){
                            JdbcInterceptOperateHandler jdbcInterceptOperateHandler = new JdbcInterceptOperateHandler(mappedStatement.getConfiguration().getEnvironment().getDataSource());
                            Object jdbcData = jdbcInterceptOperateHandler.jdbcQueryHandleForCondition(boundSql, conditionList, cacheProperty);
                            if(jdbcData!= null){
                                 if(cacheData!= null && cacheData instanceof List){
                                    List resultDataList = (List) cacheData;
                                     List allResultList = new LinkedList(resultDataList);
                                     if(jdbcData instanceof List){
                                         allResultList.addAll((List)jdbcData);
                                     }else{
                                         allResultList.add(jdbcData);
                                     }
                                     cacheData = allResultList;
                                }else{
                                     cacheData = jdbcData;
                                 }
                                 //同步DB的数据到Redis缓存
                                cacheProperty.setId(conditionList.get(0).getValue());
                                RedisCacheProperty targetCache = new RedisCacheProperty();
                                BeanUtils.copyProperties(cacheProperty,targetCache);
                                 //处理二级空缓存
                                 if(jdbcData instanceof List && ((List)jdbcData).size() == 0){
                                     SecondLevelCacheStrategy.setDataType(targetCache);
                                     SecondLevelCacheStrategy.setExpireTime(targetCache);
                                 }
                                redisOperateCommand.saveRedisData(Arrays.asList(targetCache),jdbcData);
                            }

                        }
                        cacheData = filterResultRecord(cacheData,cacheProperty);
                    }

                }
                if(!(cacheData instanceof List)){
                    return Arrays.asList(cacheData);
                }
                return cacheData;
            }
            //如果是更新语句
            else{
                MAPPER_STATEMENT_HOLDER.set(mappedStatement);
                result = invocation.proceed();
            }
        }
        return result;
    }

    /**
     * 同步DB数据到redis
     * @param mappedStatement
     * @param boundSql
     * @param invocation
     * @param cacheProperty
     * @return
     * @throws Exception
     */
    private Object saveRedisData(MappedStatement mappedStatement,BoundSql boundSql,Invocation invocation,RedisCacheProperty cacheProperty) throws Exception {
        Object cacheData = null;
        //基于最大请求数的限流操作
        if(DefaultCacheOptions.LimitStrategy.BASED_REQUEST.toString()
                .equalsIgnoreCase(cacheProperty.getLimitStrategy())){
            cacheData = setRequestLimitLatch(cacheProperty,mappedStatement,boundSql);
        }

        //基于二级缓存
        if(DefaultCacheOptions.LimitStrategy.BASED_SECOND_CACHE.toString()
                .equalsIgnoreCase(cacheProperty.getLimitStrategy())){
            cacheData = setSecondLevelCacheLimit(cacheProperty,mappedStatement,boundSql);
        }
        return cacheData;
    }


    /**
     * 设置请求限流，防止缓存雪崩
     * @param cacheProperty
     * @param mappedStatement
     * @param boundSql
     * @return
     */
  private Object setRequestLimitLatch(RedisCacheProperty cacheProperty,MappedStatement mappedStatement,
                                      BoundSql boundSql){
      Object cacheData = null;
      //基于请求限流降级操作
      if(RequestLimitLatchStrategy.checkCurrentRequestThreshold(cacheProperty)){
          //查询DB
          JdbcInterceptOperateHandler jdbcInterceptOperateHandler = new JdbcInterceptOperateHandler(mappedStatement.getConfiguration().getEnvironment().getDataSource());
          cacheData = jdbcInterceptOperateHandler.jdbcQueryOperateHandle(boundSql, cacheProperty);
          boolean checkFlag = (cacheData != null && !(cacheData instanceof List))
                  || ((cacheData!= null && cacheData instanceof List) && !CollectionUtils.isEmpty((List)cacheData));
          //DB查询不为空保存到Redis-->针对主动缓存的
          if(checkFlag){
              redisOperateCommand.saveRedisData(Arrays.asList(cacheProperty),cacheData);
          }
      }else{
          cacheData = Collections.EMPTY_LIST;   //降级处理
      }
      return cacheData;
  }

    /**
     * 设置二级缓存限流操作，防止缓存穿透
     * @param cacheProperty
     * @param mappedStatement
     * @param boundSql
     * @return
     */
  private Object setSecondLevelCacheLimit(RedisCacheProperty cacheProperty,MappedStatement mappedStatement,
                                          BoundSql boundSql){
      Object cacheData = null;
      //查询DB
      JdbcInterceptOperateHandler jdbcInterceptOperateHandler = new JdbcInterceptOperateHandler(mappedStatement.getConfiguration().getEnvironment().getDataSource());
      cacheData = jdbcInterceptOperateHandler.jdbcQueryOperateHandle(boundSql, cacheProperty);
      //针对空对象缓存优化治理,保存二级缓存
      if(cacheData == null || (cacheData!= null && cacheData instanceof List && ((List)cacheData).size() == 0)){
          redisOperateCommand.saveSecondCacheData(cacheProperty,Collections.EMPTY_LIST);
      }else{
          redisOperateCommand.saveRedisData(Arrays.asList(cacheProperty),cacheData);
      }
      return cacheData;
  }

    /**
     * 检查缓存控制开关
     * @return
     */
    private boolean checkRedisSwitch() {
        String cacheSwitch = RedisUtil.get(CACHE_SWITCH_KEY, String.class);
        //缓存开关关闭直接访问DB
        if(cacheSwitch == null || "off".equalsIgnoreCase(cacheSwitch)){
            return false;
        }
        return true;
    }

    private Object filterResultRecord(Object cacheData,RedisCacheProperty cacheProperty){
        List<Condition> conditionList = cacheProperty.getConditionList();
        if(!CollectionUtils.isEmpty(conditionList)){
            cacheData =  conditionFilter(cacheData,conditionList);
        }
        return cacheData;
    }



    /**
     * 同步Redis缓存数据
     * @param cachePropertyList
     * @param paramObject
     * @param mappedStatement
     */
    private void syncRedisCacheData(List<RedisCacheProperty> cachePropertyList,Object paramObject,MappedStatement mappedStatement){
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        //对增删改操作进行处理
        switch (sqlCommandType){
            case INSERT:
                redisOperateCommand.saveRedisData(cachePropertyList,paramObject);
                break;
            case UPDATE:
                redisOperateCommand.updateRedisData(cachePropertyList.get(0),paramObject);
                break;
            case DELETE:
                redisOperateCommand.deleteRedisData(cachePropertyList);
                break;
            default:
                break;
        }

    }

    /**
     * 查询缓存
     * @param cacheProperty
     * @return
     */
    private Object queryRedisCacheData(RedisCacheProperty cacheProperty,List<Condition> conditionList){
        if(cacheProperty == null){
            return null;
        }
        Object data = null;

        //包含in语句，拆分key
        if(cacheProperty.isInConditionClause()){
            //存储需要查询DB的条件
            List<String> otherKeyList = new LinkedList<>();
            List dataList = new LinkedList();
            //如果只有一个key
            String keyProperty = cacheProperty.getId().toString();
            if(keyProperty.indexOf("_") < 0){
                String[] keyArray = keyProperty.split(",");
                Stream.of(keyArray).forEach(key ->{
                    RedisCacheProperty singleCacheProperty = new RedisCacheProperty();
                    BeanUtils.copyProperties(cacheProperty,singleCacheProperty);
                    singleCacheProperty.setId(key);
                    //查询一级缓存
                    Object resultData = redisOperateCommand.queryRedisData(singleCacheProperty);
                    //一级缓存不存在，查询二级缓存
                    if(resultData == null){
                        resultData = redisOperateCommand.querySecondCacheData(singleCacheProperty);
                    }
                    //二级缓存不存在，应该读DB(这是处理一部分在缓存一部分在DB的场景)
                    if(resultData == null){
                        otherKeyList.add(key);
                    }else{
                        if(resultData instanceof List){
                            dataList.addAll((List)resultData);
                        }else{
                            dataList.add(resultData);
                        }
                    }
                });
                //包含从DB查询的
                if(!CollectionUtils.isEmpty(otherKeyList)){
                    if(conditionList != null){
                        conditionList.add(new Condition(cacheProperty.getKeyName() + " in", StringUtils.collectionToCommaDelimitedString(otherKeyList)));
                    }
                }

               return dataList;
            }

        }else{
            //没有In语句
            data = redisOperateCommand.queryRedisData(cacheProperty);
            //查询二级缓存数据
            if(data == null){
                redisOperateCommand.querySecondCacheData(cacheProperty);
            }
        }

        if(data == null){
            return null;
        }
        if(!List.class.isAssignableFrom(data.getClass())){
            return Arrays.asList(data);
        }
        return data;
    }



    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * 条件过滤
     * @param resultData
     * @return
     */
    private Object conditionFilter(Object resultData,List<Condition> conditionList){
        String tempField = null;
        List<Object> emptyList = Collections.emptyList();
        List dataList = null;
        try {
            if(resultData instanceof List){
                List list = (List) resultData;
                if(CollectionUtils.isEmpty(list)){
                    return emptyList;
                }
                dataList = list;
            }else{
                dataList = Arrays.asList(resultData);
            }
            for(Condition condition : conditionList){
                String cond = condition.getCondition();
                if(cond.contains(">") && !cond.contains(">=")){
                    tempField = cond.substring(0,cond.indexOf(">")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.GT);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }else if(cond.contains(">=")){
                    tempField = cond.substring(0,cond.indexOf(">=")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.GTE);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }else if(cond.contains("<") && !cond.contains("<=")){
                    tempField = cond.substring(0,cond.indexOf("<")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.LT);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }else if(cond.contains("<=")){
                    tempField = cond.substring(0,cond.indexOf("<=")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.LTE);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }else if(cond.contains("=")){
                    tempField = cond.substring(0,cond.indexOf("=")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.E);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }
                else if(cond.contains("<>")){
                    tempField = cond.substring(0,cond.indexOf("<>")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.NE);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }
                else if(cond.contains("like")){
                    tempField = cond.substring(0,cond.indexOf("like")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.LIKE);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }else if(cond.contains("is null")){
                    tempField = cond.substring(0,cond.indexOf("is null")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.NULL);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }else if(cond.contains("is not null")){
                    tempField = cond.substring(0,cond.indexOf("is not null")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.NOTNULL);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }else if(cond.contains(" in")){
                    tempField = cond.substring(0,cond.indexOf("in")).trim();
                    dataList = conditionFieldFilter(dataList,tempField,condition,Condition.ConditionOperator.IN);
                    if(CollectionUtils.isEmpty(dataList)){
                        return emptyList;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("缓存条件过滤失败",e);
        }
        return dataList;
    }

    /**
     * 条件字段过滤器
     * @param dataList
     * @param tempField
     * @param condition
     * @param operator
     * @return
     */
    private List conditionFieldFilter(List dataList, String tempField, Condition condition, Condition.ConditionOperator operator){
       List resultList  = (List) dataList.stream().filter(data -> {
            boolean result = true;
            try {
                    Method method = data.getClass().getMethod("get"+ StringUtils.capitalize(tempField));
                    Object value1 = method.invoke(data);
                    Object value2 = condition.getValue();
                switch (operator) {
                    case E:
                        result = value1 == null ? false : value1.equals(value2);
                        break;
                    case NE:
                        result = value1 == null ? false :  !value1.equals(value2);
                        break;
                    case LT:
                        if(value1 == null){
                            result = false;
                        }else{
                            if(value1 instanceof Date){
                                Date source = (Date) value1;
                                Date target = (Date) value2;
                                result = source.before(target);
                            }else{
                                result = (int) value1 < (int) value2;
                            }
                        }
                        break;
                    case LTE:
                        if(value1 == null) {
                            result = false;
                        }else{
                            if (value1 instanceof Date) {
                                Date source = (Date) value1;
                                Date target = (Date) value2;
                                result = source.before(target) || DateFormatUtils.format(source, "yyyy-MM-dd").equals(DateFormatUtils.format(target, "yyyy-MM-dd"));
                            } else {
                                result = (int) value1 <= (int) value2;
                            }
                        }
                        break;
                    case GT:
                        if(value1 == null) {
                            result = false;
                        }else {
                            if (value1 instanceof Date) {
                                Date source = (Date) value1;
                                Date target = (Date) value2;
                                result = source.after(target);
                            } else {
                                result = (int) value1 > (int) value2;
                            }
                        }
                        break;
                    case GTE:
                        if(value1 == null) {
                            result = false;
                        }else {
                            if (value1 instanceof Date) {
                                Date source = (Date) value1;
                                Date target = (Date) value2;
                                result = source.after(target) || DateFormatUtils.format(source, "yyyy-MM-dd").equals(DateFormatUtils.format(target, "yyyy-MM-dd"));
                            } else {
                                result = (int) value1 >= (int) value2;
                            }
                        }
                        break;
                    case LIKE:
                        if(value1 == null) {
                            result = false;
                        }else {
                            result = value1.toString().contains(value2.toString());
                        }
                        break;
                    case NULL:
                        result = value1 == null;
                        break;
                    case NOTNULL:
                        result = value1 != null;
                        break;
                    case IN:
                        if(value1 == null) {
                            result = false;
                        }else {
                            Object value = value2;
                            List list = (List) value;
                            result = list.stream().anyMatch(tempData -> tempData.equals(value1));
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LOGGER.error("调用方法conditionFieldFilter过滤失败", e);
            }
            return result;

        }).collect(Collectors.toList());

        return resultList;
    }
}
