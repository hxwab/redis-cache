package com.ctrip.db.cache.redis;

import com.alibaba.fastjson.serializer.PropertyFilter;

import java.util.Arrays;

/**
 * JSON 序列化过滤器
 * Created by zhao.yong on 2017/10/11.
 */
public class FastJsonPropertyFilter implements PropertyFilter {
    /**
     * json序列化排除的字段
     */
    private String[] excludeColumns;

    public FastJsonPropertyFilter(String... excludeColumns){
          this.excludeColumns = excludeColumns;
    }

    @Override
    public boolean apply(Object object, String name, Object value) {
        if(excludeColumns!= null && excludeColumns.length > 0){
           if( Arrays.asList(excludeColumns).contains(name)){
                return false;
           }
        }
        return true;
    }
}
