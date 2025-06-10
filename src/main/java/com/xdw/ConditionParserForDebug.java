package com.xdw;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 条件解析器 - 调试版本
 * 
 * 这个版本包含大量的调试输出信息，帮助理解每一步的处理过程
 * 如果你的公司使用类似的QueryWrapper但有细节差异，可以通过这些输出快速定位问题
 * 
 * 使用建议：
 * 1. 先运行简单的测试用例，观察输出格式
 * 2. 对比你公司的QueryWrapper生成的SQL格式
 * 3. 根据差异调整相应的方法调用
 */
public class ConditionParserForDebug {
    
    // 控制调试输出的开关
    private static boolean DEBUG_ENABLED = true;
    
    /**
     * 主解析方法
     * @param condition SQL条件字符串，如 "name = 'John' AND age > 18"
     * @param queryWrapper 你的QueryWrapper实例
     * @return 处理后的QueryWrapper
     */
    public static <T> QueryWrapper<T> parse(String condition, QueryWrapper<T> queryWrapper) {
        debugPrint("================== 开始解析 SQL 条件 ==================");
        debugPrint("原始条件: " + condition);
        
        try {
            // 去除前后空格并检查空字符串
            condition = condition.trim();
            if (condition.isEmpty()) {
                debugPrint("条件为空，直接返回");
                return queryWrapper;
            }
            
            debugPrint("准备使用 Druid 解析器解析条件...");
            
            // 使用Druid解析器将字符串解析为AST（抽象语法树）
            SQLExpr sqlExpr = SQLUtils.toSQLExpr(condition, DbType.mysql);
            debugPrint("Druid 解析成功，AST 根节点类型: " + sqlExpr.getClass().getSimpleName());
            debugPrint("AST 根节点完整类名: " + sqlExpr.getClass().getName());
            
            // 递归处理AST
            parseSQLExpr(sqlExpr, queryWrapper, 0);
            
            debugPrint("解析完成，最终生成的 SQL 片段: " + queryWrapper.getSqlSegment());
            debugPrint("参数映射: " + queryWrapper.getParamNameValuePairs());
            debugPrint("================== 解析结束 ==================\n");
            
            return queryWrapper;
        } catch (Exception e) {
            debugPrint("解析失败: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Failed to parse condition: " + condition, e);
        }
    }

    /**
     * 递归处理SQL表达式
     * @param expr SQL表达式节点
     * @param queryWrapper QueryWrapper实例
     * @param depth 递归深度，用于缩进显示
     */
    private static <T> void parseSQLExpr(SQLExpr expr, QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        debugPrint(indent + "处理表达式: " + expr.getClass().getSimpleName());
        debugPrint(indent + "表达式内容: " + expr.toString());
        
        if (expr instanceof SQLBinaryOpExpr) {
            debugPrint(indent + "识别为二元操作表达式");
            handleBinaryOpExpr((SQLBinaryOpExpr) expr, queryWrapper, depth + 1);
        } else if (expr instanceof SQLInListExpr) {
            debugPrint(indent + "识别为 IN 列表表达式");
            handleInListExpr((SQLInListExpr) expr, queryWrapper, depth + 1);
        } else if (expr instanceof SQLBetweenExpr) {
            debugPrint(indent + "识别为 BETWEEN 表达式");
            handleBetweenExpr((SQLBetweenExpr) expr, queryWrapper, depth + 1);
        } else if (expr instanceof SQLNotExpr) {
            debugPrint(indent + "识别为 NOT 表达式");
            handleNotExpr((SQLNotExpr) expr, queryWrapper, depth + 1);
        } else if (expr instanceof SQLUnaryExpr) {
            debugPrint(indent + "识别为一元操作表达式");
            handleUnaryExpr((SQLUnaryExpr) expr, queryWrapper, depth + 1);
        } else {
            debugPrint(indent + "未支持的表达式类型: " + expr.getClass().getName());
            throw new UnsupportedOperationException("Unsupported expression type: " + expr.getClass().getName());
        }
    }

    /**
     * 处理二元操作表达式（如 =, >, <, AND, OR 等）
     */
    private static <T> void handleBinaryOpExpr(SQLBinaryOpExpr binaryOpExpr, QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        SQLBinaryOperator operator = binaryOpExpr.getOperator();
        SQLExpr left = binaryOpExpr.getLeft();
        SQLExpr right = binaryOpExpr.getRight();

        debugPrint(indent + "二元操作符: " + operator);
        debugPrint(indent + "操作符名称: " + operator.getName());
        debugPrint(indent + "左侧表达式: " + left.toString() + " (类型: " + left.getClass().getSimpleName() + ")");
        debugPrint(indent + "右侧表达式: " + right.toString() + " (类型: " + right.getClass().getSimpleName() + ")");

        switch (operator) {
            case BooleanAnd:
                debugPrint(indent + "处理 AND 逻辑...");
                // 对于AND操作，直接递归处理左右表达式
                debugPrint(indent + "处理 AND 左侧:");
                parseSQLExpr(left, queryWrapper, depth + 1);
                debugPrint(indent + "处理 AND 右侧:");
                parseSQLExpr(right, queryWrapper, depth + 1);
                debugPrint(indent + "AND 处理完成");
                break;
                
            case BooleanOr:
                debugPrint(indent + "处理 OR 逻辑...");
                debugPrint(indent + "注意：OR 操作需要特殊处理，使用 nested() 方法包装");
                // 对于OR操作，使用特殊的包装方式
                queryWrapper.and(wrapper -> {
                    debugPrint(indent + "创建 OR 左侧嵌套条件:");
                    wrapper.nested(w1 -> parseSQLExpr(left, w1, depth + 2))
                           .or()
                           .nested(w2 -> {
                               debugPrint(indent + "创建 OR 右侧嵌套条件:");
                               parseSQLExpr(right, w2, depth + 2);
                           });
                });
                debugPrint(indent + "OR 处理完成");
                break;
                
            case Equality:
            case NotEqual:
            case LessThan:
            case LessThanOrEqual:
            case GreaterThan:
            case GreaterThanOrEqual:
            case Like:
            case NotLike:
                debugPrint(indent + "处理比较操作: " + operator.getName());
                handleComparison(operator, left, right, queryWrapper, depth + 1);
                break;
                
            case Is:
            case IsNot:
                debugPrint(indent + "处理 NULL 检查: " + operator.getName());
                handleNullCheck(operator, left, right, queryWrapper, depth + 1);
                break;
                
            default:
                debugPrint(indent + "不支持的操作符: " + operator + " (" + operator.getName() + ")");
                throw new UnsupportedOperationException("Unsupported operator: " + operator.getName());
        }
        
        // 显示当前QueryWrapper状态
        debugPrint(indent + "当前 SQL 片段: " + queryWrapper.getSqlSegment());
        debugPrint(indent + "当前参数: " + queryWrapper.getParamNameValuePairs());
    }

    /**
     * 处理具体的比较操作
     */
    private static <T> void handleComparison(SQLBinaryOperator operator, SQLExpr left, SQLExpr right, 
                                           QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        debugPrint(indent + "开始提取字段名和值...");
        
        String column = extractColumnName(left, depth + 1);
        Object value = extractValue(right, depth + 1);
        
        debugPrint(indent + "提取结果 - 字段: '" + column + "', 值: '" + value + "' (类型: " + 
                  (value != null ? value.getClass().getSimpleName() : "null") + ")");

        debugPrint(indent + "开始调用 QueryWrapper 方法...");
        
        switch (operator) {
            case Equality:
                debugPrint(indent + "调用 queryWrapper.eq(\"" + column + "\", " + value + ")");
                queryWrapper.eq(column, value);
                break;
            case NotEqual:
                debugPrint(indent + "调用 queryWrapper.ne(\"" + column + "\", " + value + ")");
                queryWrapper.ne(column, value);
                break;
            case LessThan:
                debugPrint(indent + "调用 queryWrapper.lt(\"" + column + "\", " + value + ")");
                queryWrapper.lt(column, value);
                break;
            case LessThanOrEqual:
                debugPrint(indent + "调用 queryWrapper.le(\"" + column + "\", " + value + ")");
                queryWrapper.le(column, value);
                break;
            case GreaterThan:
                debugPrint(indent + "调用 queryWrapper.gt(\"" + column + "\", " + value + ")");
                queryWrapper.gt(column, value);
                break;
            case GreaterThanOrEqual:
                debugPrint(indent + "调用 queryWrapper.ge(\"" + column + "\", " + value + ")");
                queryWrapper.ge(column, value);
                break;
            case Like:
                debugPrint(indent + "处理 LIKE 操作...");
                handleLikeOperation(column, value, queryWrapper, false, depth + 1);
                break;
            case NotLike:
                debugPrint(indent + "处理 NOT LIKE 操作...");
                handleLikeOperation(column, value, queryWrapper, true, depth + 1);
                break;
            default:
                debugPrint(indent + "不支持的比较操作符: " + operator.getName());
                throw new UnsupportedOperationException("Unsupported comparison operator: " + operator.getName());
        }
        
        debugPrint(indent + "比较操作完成，当前 SQL: " + queryWrapper.getSqlSegment());
    }

    /**
     * 处理 LIKE 操作的特殊逻辑
     * 这是一个关键方法，因为不同的QueryWrapper实现对LIKE的处理可能不同
     */
    private static <T> void handleLikeOperation(String column, Object value, QueryWrapper<T> queryWrapper, 
                                              boolean isNotLike, int depth) {
        String indent = getIndent(depth);
        String valueStr = value.toString();
        
        debugPrint(indent + "LIKE 操作详细分析:");
        debugPrint(indent + "字段: " + column);
        debugPrint(indent + "原始值: '" + valueStr + "'");
        debugPrint(indent + "是否为 NOT LIKE: " + isNotLike);
        debugPrint(indent + "检查值是否包含通配符...");
        
        boolean hasWildcards = valueStr.contains("%") || valueStr.contains("_");
        debugPrint(indent + "包含通配符 % 或 _: " + hasWildcards);
        
        if (hasWildcards) {
            debugPrint(indent + "值已包含通配符，使用 apply() 方法避免双重通配符");
            if (isNotLike) {
                String sql = column + " NOT LIKE {0}";
                debugPrint(indent + "调用 queryWrapper.apply(\"" + sql + "\", \"" + valueStr + "\")");
                queryWrapper.apply(sql, valueStr);
            } else {
                String sql = column + " LIKE {0}";
                debugPrint(indent + "调用 queryWrapper.apply(\"" + sql + "\", \"" + valueStr + "\")");
                queryWrapper.apply(sql, valueStr);
            }
        } else {
            debugPrint(indent + "值不包含通配符，使用标准 like()/notLike() 方法（会自动添加 %）");
            if (isNotLike) {
                debugPrint(indent + "调用 queryWrapper.notLike(\"" + column + "\", \"" + valueStr + "\")");
                queryWrapper.notLike(column, valueStr);
                debugPrint(indent + "注意：notLike() 方法会自动将值变为 %" + valueStr + "%");
            } else {
                debugPrint(indent + "调用 queryWrapper.like(\"" + column + "\", \"" + valueStr + "\")");
                queryWrapper.like(column, valueStr);
                debugPrint(indent + "注意：like() 方法会自动将值变为 %" + valueStr + "%");
            }
        }
        
        debugPrint(indent + "LIKE 操作完成");
    }

    /**
     * 处理 NULL 检查操作
     */
    private static <T> void handleNullCheck(SQLBinaryOperator operator, SQLExpr left, SQLExpr right, 
                                          QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        debugPrint(indent + "NULL 检查操作:");
        debugPrint(indent + "右侧表达式类型: " + right.getClass().getSimpleName());
        
        if (right instanceof SQLNullExpr) {
            String column = extractColumnName(left, depth + 1);
            debugPrint(indent + "确认为 NULL 检查，字段: " + column);
            
            if (operator == SQLBinaryOperator.Is) {
                debugPrint(indent + "调用 queryWrapper.isNull(\"" + column + "\")");
                queryWrapper.isNull(column);
            } else if (operator == SQLBinaryOperator.IsNot) {
                debugPrint(indent + "调用 queryWrapper.isNotNull(\"" + column + "\")");
                queryWrapper.isNotNull(column);
            }
        } else {
            debugPrint(indent + "错误：不是有效的 NULL 检查表达式");
            throw new IllegalArgumentException("Invalid NULL check expression");
        }
    }

    /**
     * 从表达式中提取字段名
     */
    private static String extractColumnName(SQLExpr expr, int depth) {
        String indent = getIndent(depth);
        debugPrint(indent + "提取字段名，表达式类型: " + expr.getClass().getSimpleName());
        debugPrint(indent + "表达式内容: " + expr.toString());
        
        if (expr instanceof SQLIdentifierExpr) {
            String name = ((SQLIdentifierExpr) expr).getName();
            debugPrint(indent + "提取到标识符字段名: " + name);
            return name;
        } else if (expr instanceof SQLPropertyExpr) {
            String name = ((SQLPropertyExpr) expr).getName();
            debugPrint(indent + "提取到属性字段名: " + name);
            return name;
        }
        
        debugPrint(indent + "无效的字段表达式类型");
        throw new IllegalArgumentException("Invalid column expression: " + expr);
    }

    /**
     * 从表达式中提取值
     */
    private static Object extractValue(SQLExpr expr, int depth) {
        String indent = getIndent(depth);
        debugPrint(indent + "提取值，表达式类型: " + expr.getClass().getSimpleName());
        debugPrint(indent + "表达式内容: " + expr.toString());
        
        if (expr instanceof SQLCharExpr) {
            String text = ((SQLCharExpr) expr).getText();
            debugPrint(indent + "提取到字符串值: '" + text + "'");
            return text;
        } else if (expr instanceof SQLIntegerExpr) {
            int value = ((SQLIntegerExpr) expr).getNumber().intValue();
            debugPrint(indent + "提取到整数值: " + value);
            return value;
        } else if (expr instanceof SQLNumberExpr) {
            Number number = ((SQLNumberExpr) expr).getNumber();
            debugPrint(indent + "提取到数字值: " + number + " (类型: " + number.getClass().getSimpleName() + ")");
            return number;
        } else if (expr instanceof SQLBooleanExpr) {
            boolean value = ((SQLBooleanExpr) expr).getBooleanValue();
            debugPrint(indent + "提取到布尔值: " + value);
            return value;
        } else if (expr instanceof SQLNullExpr) {
            debugPrint(indent + "提取到 NULL 值");
            return null;
        } else if (expr instanceof SQLMethodInvokeExpr) {
            debugPrint(indent + "提取函数调用值...");
            Object result = handleFunction((SQLMethodInvokeExpr) expr, depth + 1);
            debugPrint(indent + "函数结果: " + result);
            return result;
        } else if (expr instanceof SQLNCharExpr) {
            String text = ((SQLNCharExpr) expr).getText();
            debugPrint(indent + "提取到 NCHAR 字符串值: '" + text + "'");
            return text;
        }
        
        debugPrint(indent + "不支持的值类型: " + expr.getClass().getName());
        throw new UnsupportedOperationException("Unsupported value type: " + expr.getClass().getName());
    }

    /**
     * 处理函数调用
     */
    private static Object handleFunction(SQLMethodInvokeExpr func, int depth) {
        String indent = getIndent(depth);
        String methodName = func.getMethodName().toLowerCase();
        debugPrint(indent + "处理函数: " + methodName);
        debugPrint(indent + "参数个数: " + func.getArguments().size());
        
        switch (methodName) {
            case "now":
            case "sysdate":
                debugPrint(indent + "返回当前时间");
                return new Date();
            case "current_date":
                debugPrint(indent + "返回当前日期");
                return new java.sql.Date(System.currentTimeMillis());
            case "current_timestamp":
                debugPrint(indent + "返回当前时间戳");
                return new java.sql.Timestamp(System.currentTimeMillis());
            case "concat":
                debugPrint(indent + "处理字符串连接函数");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < func.getArguments().size(); i++) {
                    SQLExpr arg = func.getArguments().get(i);
                    Object value = extractValue(arg, depth + 1);
                    debugPrint(indent + "连接参数 " + (i + 1) + ": " + value);
                    if (value != null) {
                        sb.append(value.toString());
                    }
                }
                String result = sb.toString();
                debugPrint(indent + "连接结果: '" + result + "'");
                return result;
            default:
                debugPrint(indent + "不支持的函数: " + methodName);
                throw new UnsupportedOperationException("Unsupported function: " + methodName);
        }
    }

    /**
     * 处理 IN 列表表达式
     */
    private static <T> void handleInListExpr(SQLInListExpr inListExpr, QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        String column = extractColumnName(inListExpr.getExpr(), depth + 1);
        boolean isNot = inListExpr.isNot();
        
        debugPrint(indent + "IN 操作:");
        debugPrint(indent + "字段: " + column);
        debugPrint(indent + "是否为 NOT IN: " + isNot);
        debugPrint(indent + "值列表长度: " + inListExpr.getTargetList().size());
        
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < inListExpr.getTargetList().size(); i++) {
            SQLExpr valueExpr = inListExpr.getTargetList().get(i);
            Object value = extractValue(valueExpr, depth + 2);
            debugPrint(indent + "值 " + (i + 1) + ": " + value);
            values.add(value);
        }

        if (isNot) {
            debugPrint(indent + "调用 queryWrapper.notIn(\"" + column + "\", " + values + ")");
            queryWrapper.notIn(column, values);
        } else {
            debugPrint(indent + "调用 queryWrapper.in(\"" + column + "\", " + values + ")");
            queryWrapper.in(column, values);
        }
    }

    /**
     * 处理 BETWEEN 表达式
     */
    private static <T> void handleBetweenExpr(SQLBetweenExpr betweenExpr, QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        String column = extractColumnName(betweenExpr.getTestExpr(), depth + 1);
        Object begin = extractValue(betweenExpr.getBeginExpr(), depth + 1);
        Object end = extractValue(betweenExpr.getEndExpr(), depth + 1);
        boolean isNot = betweenExpr.isNot();
        
        debugPrint(indent + "BETWEEN 操作:");
        debugPrint(indent + "字段: " + column);
        debugPrint(indent + "起始值: " + begin);
        debugPrint(indent + "结束值: " + end);
        debugPrint(indent + "是否为 NOT BETWEEN: " + isNot);

        if (isNot) {
            debugPrint(indent + "调用 queryWrapper.notBetween(\"" + column + "\", " + begin + ", " + end + ")");
            queryWrapper.notBetween(column, begin, end);
        } else {
            debugPrint(indent + "调用 queryWrapper.between(\"" + column + "\", " + begin + ", " + end + ")");
            queryWrapper.between(column, begin, end);
        }
    }

    /**
     * 处理 NOT 表达式
     */
    private static <T> void handleNotExpr(SQLNotExpr notExpr, QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        debugPrint(indent + "NOT 操作，包装内部表达式");
        queryWrapper.not(wrapper -> parseSQLExpr(notExpr.getExpr(), wrapper, depth + 1));
    }

    /**
     * 处理一元表达式
     */
    private static <T> void handleUnaryExpr(SQLUnaryExpr unaryExpr, QueryWrapper<T> queryWrapper, int depth) {
        String indent = getIndent(depth);
        debugPrint(indent + "一元操作符: " + unaryExpr.getOperator());
        
        if (unaryExpr.getOperator() == SQLUnaryOperator.Not) {
            debugPrint(indent + "处理一元 NOT 操作");
            queryWrapper.not(wrapper -> parseSQLExpr(unaryExpr.getExpr(), wrapper, depth + 1));
        } else {
            debugPrint(indent + "不支持的一元操作符: " + unaryExpr.getOperator());
            throw new UnsupportedOperationException("Unsupported unary operator: " + unaryExpr.getOperator());
        }
    }

    // ==================== 辅助方法 ====================
    
    /**
     * 生成缩进字符串，用于格式化输出
     */
    private static String getIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
    
    /**
     * 调试输出方法
     */
    private static void debugPrint(String message) {
        if (DEBUG_ENABLED) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    /**
     * 设置是否启用调试输出
     */
    public static void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED = enabled;
    }
    
    /**
     * 检查是否启用调试输出
     */
    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }
}