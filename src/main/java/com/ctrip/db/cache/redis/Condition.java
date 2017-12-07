package com.ctrip.db.cache.redis;

/**
 * 条件过滤器
 * Created by zhao.yong on 2017/10/12.
 */
public class Condition {

    /**
     * 条件表达式
     */
    private String condition;
    /**
     * 条件值
     */
    private Object value;

    public Condition(String condition, Object value) {
        this.condition = condition;
        this.value = value;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static enum ConditionOperator{
        E,         //=
        NE,        //!=
        LT,        //<
        GT,        //>
        LTE,       //<=
        GTE,       //>=
        LIKE,      //like
        IN,        //in
        NULL,      //null
        NOTNULL,   //notnull
    }
}
