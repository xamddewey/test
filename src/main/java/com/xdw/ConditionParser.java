package com.xdw;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConditionParser {

    public static <T> QueryWrapper<T> parse(String condition, QueryWrapper<T> queryWrapper) {
        try {
            // 去除前后空格并检查空字符串
            condition = condition.trim();
            if (condition.isEmpty()) {
                return queryWrapper;
            }
            
            SQLExpr sqlExpr = SQLUtils.toSQLExpr(condition, DbType.mysql);
            parseSQLExpr(sqlExpr, queryWrapper);
            return queryWrapper;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse condition: " + condition, e);
        }
    }

    private static <T> void parseSQLExpr(SQLExpr expr, QueryWrapper<T> queryWrapper) {
        if (expr instanceof SQLBinaryOpExpr) {
            handleBinaryOpExpr((SQLBinaryOpExpr) expr, queryWrapper);
        } else if (expr instanceof SQLInListExpr) {
            handleInListExpr((SQLInListExpr) expr, queryWrapper);
        } else if (expr instanceof SQLBetweenExpr) {
            handleBetweenExpr((SQLBetweenExpr) expr, queryWrapper);
        } else if (expr instanceof SQLNotExpr) {
            handleNotExpr((SQLNotExpr) expr, queryWrapper);
        } else if (expr instanceof SQLUnaryExpr) {
            handleUnaryExpr((SQLUnaryExpr) expr, queryWrapper);
        } else {
            throw new UnsupportedOperationException("Unsupported expression type: " + expr.getClass().getName());
        }
    }

    private static <T> void handleBinaryOpExpr(SQLBinaryOpExpr binaryOpExpr, QueryWrapper<T> queryWrapper) {
        SQLBinaryOperator operator = binaryOpExpr.getOperator();
        SQLExpr left = binaryOpExpr.getLeft();
        SQLExpr right = binaryOpExpr.getRight();

        switch (operator) {
            case BooleanAnd:
                // 对于AND操作，直接递归处理左右表达式
                parseSQLExpr(left, queryWrapper);
                parseSQLExpr(right, queryWrapper);
                break;
            case BooleanOr:
                // 对于OR操作，使用or()方法包装
                queryWrapper.and(wrapper -> {
                    wrapper.nested(w1 -> parseSQLExpr(left, w1))
                           .or()
                           .nested(w2 -> parseSQLExpr(right, w2));
                });
                break;
            case Equality:
            case NotEqual:
            case LessThan:
            case LessThanOrEqual:
            case GreaterThan:
            case GreaterThanOrEqual:
            case Like:
            case NotLike:
                handleComparison(operator, left, right, queryWrapper);
                break;
            case Is:
            case IsNot:
                handleNullCheck(operator, left, right, queryWrapper);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator.getName());
        }
    }

    private static <T> void handleComparison(SQLBinaryOperator operator, SQLExpr left, SQLExpr right, QueryWrapper<T> queryWrapper) {
        String column = extractColumnName(left);
        Object value = extractValue(right);

        switch (operator) {
            case Equality:
                queryWrapper.eq(column, value);
                break;
            case NotEqual:
                queryWrapper.ne(column, value);
                break;
            case LessThan:
                queryWrapper.lt(column, value);
                break;
            case LessThanOrEqual:
                queryWrapper.le(column, value);
                break;
            case GreaterThan:
                queryWrapper.gt(column, value);
                break;
            case GreaterThanOrEqual:
                queryWrapper.ge(column, value);
                break;
            case Like:
                handleLikeOperation(column, value, queryWrapper, false);
                break;
            case NotLike:
                handleLikeOperation(column, value, queryWrapper, true);
                break;
        }
    }

    /**
     * 处理 LIKE 操作
     * MyBatis-Plus 的 like() 方法会自动添加 %，但如果 SQL 中已经包含通配符，
     * 我们需要使用 apply() 方法直接设置条件以避免双重的 %
     */
    private static <T> void handleLikeOperation(String column, Object value, QueryWrapper<T> queryWrapper, boolean isNotLike) {
        String valueStr = value.toString();
        
        // 检查值是否已经包含通配符
        if (valueStr.contains("%") || valueStr.contains("_")) {
            // 如果已经包含通配符，使用 apply 方法直接写 SQL
            if (isNotLike) {
                queryWrapper.apply(column + " NOT LIKE {0}", valueStr);
            } else {
                queryWrapper.apply(column + " LIKE {0}", valueStr);
            }
        } else {
            // 如果不包含通配符，使用 MyBatis-Plus 的方法（会自动添加 %）
            if (isNotLike) {
                queryWrapper.notLike(column, valueStr);
            } else {
                queryWrapper.like(column, valueStr);
            }
        }
    }

    private static <T> void handleNullCheck(SQLBinaryOperator operator, SQLExpr left, SQLExpr right, QueryWrapper<T> queryWrapper) {
        if (right instanceof SQLNullExpr) {
            String column = extractColumnName(left);
            if (operator == SQLBinaryOperator.Is) {
                queryWrapper.isNull(column);
            } else if (operator == SQLBinaryOperator.IsNot) {
                queryWrapper.isNotNull(column);
            }
        } else {
            throw new IllegalArgumentException("Invalid NULL check expression");
        }
    }

    private static String extractColumnName(SQLExpr expr) {
        if (expr instanceof SQLIdentifierExpr) {
            return ((SQLIdentifierExpr) expr).getName();
        } else if (expr instanceof SQLPropertyExpr) {
            return ((SQLPropertyExpr) expr).getName();
        }
        throw new IllegalArgumentException("Invalid column expression: " + expr);
    }

    private static Object extractValue(SQLExpr expr) {
        if (expr instanceof SQLCharExpr) {
            return ((SQLCharExpr) expr).getText();
        } else if (expr instanceof SQLIntegerExpr) {
            return ((SQLIntegerExpr) expr).getNumber().intValue();
        } else if (expr instanceof SQLNumberExpr) {
            return ((SQLNumberExpr) expr).getNumber();
        } else if (expr instanceof SQLBooleanExpr) {
            return ((SQLBooleanExpr) expr).getBooleanValue();
        } else if (expr instanceof SQLNullExpr) {
            return null;
        } else if (expr instanceof SQLMethodInvokeExpr) {
            return handleFunction((SQLMethodInvokeExpr) expr);
        } else if (expr instanceof SQLNCharExpr) {
            return ((SQLNCharExpr) expr).getText();
        }
        throw new UnsupportedOperationException("Unsupported value type: " + expr.getClass().getName());
    }

    private static Object handleFunction(SQLMethodInvokeExpr func) {
        String methodName = func.getMethodName().toLowerCase();
        switch (methodName) {
            case "now":
            case "sysdate":
                return new Date();
            case "current_date":
                return new java.sql.Date(System.currentTimeMillis());
            case "current_timestamp":
                return new java.sql.Timestamp(System.currentTimeMillis());
            case "concat":
                StringBuilder sb = new StringBuilder();
                for (SQLExpr arg : func.getArguments()) {
                    Object value = extractValue(arg);
                    if (value != null) {
                        sb.append(value.toString());
                    }
                }
                return sb.toString();
            default:
                throw new UnsupportedOperationException("Unsupported function: " + methodName);
        }
    }

    private static <T> void handleInListExpr(SQLInListExpr inListExpr, QueryWrapper<T> queryWrapper) {
        String column = extractColumnName(inListExpr.getExpr());
        List<Object> values = new ArrayList<>();

        for (SQLExpr valueExpr : inListExpr.getTargetList()) {
            values.add(extractValue(valueExpr));
        }

        if (inListExpr.isNot()) {
            queryWrapper.notIn(column, values);
        } else {
            queryWrapper.in(column, values);
        }
    }

    private static <T> void handleBetweenExpr(SQLBetweenExpr betweenExpr, QueryWrapper<T> queryWrapper) {
        String column = extractColumnName(betweenExpr.getTestExpr());
        Object begin = extractValue(betweenExpr.getBeginExpr());
        Object end = extractValue(betweenExpr.getEndExpr());

        if (betweenExpr.isNot()) {
            queryWrapper.notBetween(column, begin, end);
        } else {
            queryWrapper.between(column, begin, end);
        }
    }

    private static <T> void handleNotExpr(SQLNotExpr notExpr, QueryWrapper<T> queryWrapper) {
        queryWrapper.not(wrapper -> parseSQLExpr(notExpr.getExpr(), wrapper));
    }

    private static <T> void handleUnaryExpr(SQLUnaryExpr unaryExpr, QueryWrapper<T> queryWrapper) {
        if (unaryExpr.getOperator() == SQLUnaryOperator.Not) {
            queryWrapper.not(wrapper -> parseSQLExpr(unaryExpr.getExpr(), wrapper));
        } else {
            // 对于正负号等一元操作符，通常在值提取时处理
            throw new UnsupportedOperationException("Unsupported unary operator: " + unaryExpr.getOperator());
        }
    }
}