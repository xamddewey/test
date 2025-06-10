package com.xdw;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionParserTest {

    static class User {
        // 测试用实体类
        String fieldA;
        String fieldB;
        String fieldC;
        String fieldD;
    }

    @Test
    public void testSimpleCondition() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "fieldA = 'valueA'";
        ConditionParser.parse(condition, wrapper);

        // 验证SQL片段和参数 - MyBatis-Plus 使用 #{ew.paramNameValuePairs.XXX} 格式
        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment, "SQL segment should not be null");
        assertTrue(sqlSegment.contains("fieldA = #{ew.paramNameValuePairs."), 
                  "SQL should contain 'fieldA = #{ew.paramNameValuePairs.', but got: " + sqlSegment);
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("valueA"));
    }

    @Test
    public void testComplexConditionWithAndOr() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "(fieldA = 'valueA' AND fieldB = 'valueB') OR fieldC = 'valueC'";
        ConditionParser.parse(condition, wrapper);

        // 验证SQL片段包含预期的部分
        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("fieldA = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("fieldB = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("fieldC = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("AND"));
        assertTrue(sqlSegment.contains("OR"));

        // 验证参数
        assertEquals(3, wrapper.getParamNameValuePairs().size());
        Map<String, Object> params = wrapper.getParamNameValuePairs();
        assertTrue(params.containsValue("valueA"));
        assertTrue(params.containsValue("valueB"));
        assertTrue(params.containsValue("valueC"));
    }

    @Test
    public void testConditionWithInOperator() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "fieldA IN ('value1', 'value2', 'value3')";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        // IN 操作也使用 MyBatis-Plus 的参数格式
        assertTrue(sqlSegment.contains("fieldA IN (#{ew.paramNameValuePairs."));
        assertEquals(3, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("value1"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("value2"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("value3"));
    }

    @Test
    public void testConditionWithBetweenOperator() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "fieldA BETWEEN 10 AND 20";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("fieldA BETWEEN #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("AND #{ew.paramNameValuePairs."));
        assertEquals(2, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue(10));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(20));
    }

    @Test
    public void testConditionWithIsNullAndIsNotNull() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "fieldA IS NULL AND fieldB IS NOT NULL";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("fieldA IS NULL"));
        assertTrue(sqlSegment.contains("fieldB IS NOT NULL"));
        assertTrue(sqlSegment.contains("AND"));
        assertEquals(0, wrapper.getParamNameValuePairs().size());
    }

    @Test
    public void testConditionWithLikeOperator() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "fieldA LIKE '%value%'";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        // 即使使用 apply() 方法，MyBatis-Plus 仍然使用自己的参数格式
        assertTrue(sqlSegment.contains("fieldA LIKE #{ew.paramNameValuePairs."), 
                   "SQL should contain 'fieldA LIKE #{ew.paramNameValuePairs.', but got: " + sqlSegment);
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        // 参数值应该保持原样，不会有双重的 %
        assertTrue(wrapper.getParamNameValuePairs().containsValue("%value%"));
    }

    @Test
    public void testConditionWithLikeOperatorNoWildcards() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "fieldA LIKE 'value'";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        // 没有通配符时，使用 MyBatis-Plus 的 like() 方法
        assertTrue(sqlSegment.contains("fieldA LIKE #{ew.paramNameValuePairs."), 
                   "SQL should contain 'fieldA LIKE #{ew.paramNameValuePairs.', but got: " + sqlSegment);
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        // MyBatis-Plus 会自动添加 %，所以应该是 %value%
        assertTrue(wrapper.getParamNameValuePairs().containsValue("%value%"));
    }

    @Test
    public void testConditionWithNotLikeOperator() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "fieldA NOT LIKE '%value%'";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("fieldA NOT LIKE #{ew.paramNameValuePairs."), 
                   "SQL should contain 'fieldA NOT LIKE #{ew.paramNameValuePairs.', but got: " + sqlSegment);
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("%value%"));
    }

    @Test
    public void testNestedConditions() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "(fieldA = 'valueA' AND (fieldB = 'valueB' OR fieldC = 'valueC')) OR fieldD = 'valueD'";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("fieldA = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("fieldB = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("fieldC = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("fieldD = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("AND"));
        assertTrue(sqlSegment.contains("OR"));

        assertEquals(4, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("valueA"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("valueB"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("valueC"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("valueD"));
    }

    @Test
    public void testNullConditions() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 测试 IS NULL
        ConditionParser.parse("email IS NULL", wrapper);
        String sqlSegment1 = wrapper.getSqlSegment();
        assertNotNull(sqlSegment1);
        assertTrue(sqlSegment1.contains("email IS NULL"));
        assertTrue(wrapper.getParamNameValuePairs().isEmpty());

        wrapper = new QueryWrapper<>();
        // 测试 IS NOT NULL
        ConditionParser.parse("phone IS NOT NULL", wrapper);
        String sqlSegment2 = wrapper.getSqlSegment();
        assertNotNull(sqlSegment2);
        assertTrue(sqlSegment2.contains("phone IS NOT NULL"));
        assertTrue(wrapper.getParamNameValuePairs().isEmpty());

        wrapper = new QueryWrapper<>();
        // 测试复杂空值组合
        ConditionParser.parse("(name IS NULL OR age IS NOT NULL) AND status = 1", wrapper);
        String sqlSegment3 = wrapper.getSqlSegment();
        assertNotNull(sqlSegment3);
        assertTrue(sqlSegment3.contains("name IS NULL"));
        assertTrue(sqlSegment3.contains("age IS NOT NULL"));
        assertTrue(sqlSegment3.contains("status = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment3.contains("OR"));
        assertTrue(sqlSegment3.contains("AND"));
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue(1));
    }

    @Test
    public void testFunctionConditions() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 测试 NOW() 函数
        ConditionParser.parse("created_at > NOW()", wrapper);
        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("created_at > #{ew.paramNameValuePairs."));
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().values().iterator().next() instanceof java.util.Date);

        wrapper = new QueryWrapper<>();
        // 测试简化的字符串比较
        ConditionParser.parse("full_name = 'test'", wrapper);
        String sqlSegment2 = wrapper.getSqlSegment();
        assertNotNull(sqlSegment2);
        assertTrue(sqlSegment2.contains("full_name = #{ew.paramNameValuePairs."));
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("test"));
    }

    @Test
    public void testEmptyCondition() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        ConditionParser.parse("", wrapper);
        String sqlSegment = wrapper.getSqlSegment();
        assertTrue(sqlSegment == null || sqlSegment.trim().isEmpty());
        assertTrue(wrapper.getParamNameValuePairs().isEmpty());
    }

    @Test
    public void testSimpleAndCondition() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "name = 'John' AND age = 25";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("name = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("age = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("AND"));
        assertEquals(2, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("John"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(25));
    }

    @Test
    public void testSimpleOrCondition() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "name = 'John' OR name = 'Jane'";
        ConditionParser.parse(condition, wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("name = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("OR"));
        assertEquals(2, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("John"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("Jane"));
    }

    // 额外添加一个功能验证测试，确保生成的 QueryWrapper 可以正常使用
    @Test
    public void testQueryWrapperUsage() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        String condition = "name = 'John' AND age > 18";
        ConditionParser.parse(condition, wrapper);

        // 验证可以继续链式调用
        wrapper.orderByDesc("created_time");
        
        String sqlSegment = wrapper.getSqlSegment();
        assertNotNull(sqlSegment);
        assertTrue(sqlSegment.contains("name = #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("age > #{ew.paramNameValuePairs."));
        assertTrue(sqlSegment.contains("ORDER BY created_time DESC"));
        assertEquals(2, wrapper.getParamNameValuePairs().size());
        assertTrue(wrapper.getParamNameValuePairs().containsValue("John"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(18));
    }
}