package com.xdw;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class TestParameterBehavior {
    
    static class User {
        // 测试用实体类
    }
    
    public static void main(String[] args) {
        System.out.println("=== 测试 MyBatis-Plus QueryWrapper 参数行为 ===");
        
        // 测试1：单个条件
        System.out.println("\n1. 单个条件测试:");
        QueryWrapper<User> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("name", "John");
        System.out.println("SQL: " + wrapper1.getSqlSegment());
        System.out.println("参数: " + wrapper1.getParamNameValuePairs());
        System.out.println("参数数量: " + wrapper1.getParamNameValuePairs().size());
        
        // 测试2：两个连续的条件
        System.out.println("\n2. 两个连续条件测试:");
        QueryWrapper<User> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("name", "John");
        System.out.println("第一个条件后 - SQL: " + wrapper2.getSqlSegment());
        System.out.println("第一个条件后 - 参数: " + wrapper2.getParamNameValuePairs());
        
        wrapper2.eq("age", 25);
        System.out.println("第二个条件后 - SQL: " + wrapper2.getSqlSegment());
        System.out.println("第二个条件后 - 参数: " + wrapper2.getParamNameValuePairs());
        System.out.println("最终参数数量: " + wrapper2.getParamNameValuePairs().size());
        
        // 测试3：使用链式调用
        System.out.println("\n3. 链式调用测试:");
        QueryWrapper<User> wrapper3 = new QueryWrapper<>();
        wrapper3.eq("name", "John").eq("age", 25);
        System.out.println("SQL: " + wrapper3.getSqlSegment());
        System.out.println("参数: " + wrapper3.getParamNameValuePairs());
        System.out.println("参数数量: " + wrapper3.getParamNameValuePairs().size());
    }
}