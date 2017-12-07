package com.ctrip.db.cache.intercept;


import com.ctrip.db.cache.annotation.RedisCacheProperty;
import com.ctrip.db.cache.redis.Condition;
import org.apache.ibatis.mapping.BoundSql;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhao.yong on 2017/11/9.
 */
public class JdbcInterceptOperateHandler {

    private JdbcTemplate jdbcTemplate;

    public JdbcInterceptOperateHandler(DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * @param boundSql
     * @param redisCacheProperty
     * @return
     */
    public Object jdbcQueryOperateHandle(BoundSql boundSql, RedisCacheProperty redisCacheProperty){
            return jdbcQueryRequest(boundSql,null,redisCacheProperty);
    }


    /**
     * @param boundSql
     * @param conditionList
     * @param redisCacheProperty
     * @return
     */
    public Object jdbcQueryHandleForCondition(BoundSql boundSql,List<Condition> conditionList,
                                              RedisCacheProperty redisCacheProperty){
        return jdbcQueryRequest(boundSql,conditionList,redisCacheProperty);
    }

    /**
     * @param boundSql
     * @param condtionList
     * @param redisCacheProperty
     * @return
     */
    private Object jdbcQueryRequest(BoundSql boundSql,List<Condition> condtionList, RedisCacheProperty redisCacheProperty){
        String sql = boundSql.getSql().toUpperCase();
        //如果包含Order by子句
        int orderByIndex = sql.indexOf("ORDER BY");
        String orderByClause = "";
        if(orderByIndex > 0){
            orderByClause = sql.substring(orderByIndex);
        }

        int index = sql.indexOf("WHERE");
        if(index > 0){
            sql = sql.substring(0,index);
        }
        StringBuilder sb = new StringBuilder(sql);
        List<Condition> allConditionList = new LinkedList<>();
        if(!CollectionUtils.isEmpty(condtionList)){
            allConditionList.addAll(condtionList);
            allConditionList.addAll(redisCacheProperty.getConditionList());
        }else{
            allConditionList =  redisCacheProperty.getKeyPropertyList();
        }
        List<Object> argsList = new LinkedList<>();
        //构建生成预编译的参数化sql
        if(!CollectionUtils.isEmpty(allConditionList)){
            sb.append(" WHERE ");
            Iterator<Condition> it = allConditionList.iterator();
            while(it.hasNext()){
                Condition condition = it.next();
                //处理in的请求
                String propertyCondition = condition.getCondition();
                if(propertyCondition.contains(" in")){
                    sb.append(" "+propertyCondition);
                    sb.append("(");
                    String[] inDataList = condition.getValue().toString().split(",");
                    List<String> inStr = new LinkedList<>();
                    for(String dataValue : inDataList){
                        inStr.add("?");
                        argsList.add(dataValue);
                    }
                    sb.append(StringUtils.collectionToCommaDelimitedString(inStr));
                    sb.append(") ");
                }else{
                    sb.append(" "+propertyCondition+" ?");
                    argsList.add(condition.getValue());
                }

                if(it.hasNext()){
                    sb.append(" AND ");
                }
            }
        }
        if(StringUtils.hasText(orderByClause)){
            sb.append(" "+orderByClause+" ");
        }
        return jdbcTemplate.query(sb.toString(),argsList.toArray(new Object[]{}),new BeanPropertyRowMapper(redisCacheProperty.getTargetClass()));
    }

}
