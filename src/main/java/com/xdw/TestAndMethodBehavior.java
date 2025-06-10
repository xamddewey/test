package com.xdw;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class TestAndMethodBehavior {
    
    static class User {
        // 测试用实体类
    }
    
    public static void main(String[] args) {
        System.out.println("=== 测试 MyBatis-Plus QueryWrapper and() 方法行为 ===");
        
        // 测试1：使用 and() 方法
        System.out.println("\n1. 使用 and() 方法:");
        QueryWrapper<User> wrapper1 = new QueryWrapper<>();
        wrapper1.and(w -> {
            w.eq("name", "John");
            w.eq("age", 25);
        });
        System.out.println("SQL: " + wrapper1.getSqlSegment());
        System.out.println("参数: " + wrapper1.getParamNameValuePairs());
        System.out.println("参数数量: " + wrapper1.getParamNameValuePairs().size());
        
        // 测试2：使用 and() 方法的另一种写法
        System.out.println("\n2. 使用 and() 方法（链式）:");
        QueryWrapper<User> wrapper2 = new QueryWrapper<>();
        wrapper2.and(w -> w.eq("name", "John").eq("age", 25));
        System.out.println("SQL: " + wrapper2.getSqlSegment());
        System.out.println("参数: " + wrapper2.getParamNameValuePairs());
        System.out.println("参数数量: " + wrapper2.getParamNameValuePairs().size());
        
        // 测试3：直接在主wrapper上链式调用
        System.out.println("\n3. 直接链式调用（对比）:");
        QueryWrapper<User> wrapper3 = new QueryWrapper<>();
        wrapper3.eq("name", "John").eq("age", 25);
        System.out.println("SQL: " + wrapper3.getSqlSegment());
        System.out.println("参数: " + wrapper3.getParamNameValuePairs());
        System.out.println("参数数量: " + wrapper3.getParamNameValuePairs().size());
    }
}