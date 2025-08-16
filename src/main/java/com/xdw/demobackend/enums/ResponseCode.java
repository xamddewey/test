package com.xdw.demobackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // ============ 成功状态 ============
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    ACCEPTED(202, "请求已接受"),
    NO_CONTENT(204, "无内容"),

    // ============ 客户端错误 ============
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数验证失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // ============ 服务器错误 ============
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ============ 业务错误码 ============
    BUSINESS_ERROR(1000, "业务处理失败"),

    // 用户相关错误
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    EMAIL_ALREADY_EXISTS(1004, "邮箱已存在"),
    USER_DISABLED(1005, "用户已被禁用"),

    // 认证相关错误
    TOKEN_EXPIRED(1101, "令牌已过期"),
    TOKEN_INVALID(1102, "令牌无效"),
    LOGIN_REQUIRED(1103, "请先登录"),
    LOGOUT_SUCCESS(1104, "登出成功"),

    // 账本相关错误
    BOOK_NOT_FOUND(1201, "账本不存在"),
    BOOK_ACCESS_DENIED(1202, "无权限访问此账本"),
    BOOK_MEMBER_EXISTS(1203, "用户已是账本成员"),
    BOOK_MEMBER_NOT_FOUND(1204, "用户不是账本成员"),
    BOOK_CREATOR_CANNOT_LEAVE(1205, "账本创建者不能离开账本"),
    BOOK_ARCHIVED(1206, "账本已归档"),
    BOOK_FROZEN(1207, "账本已冻结"),

    // 记账相关错误
    EXPENSE_NOT_FOUND(1301, "记账记录不存在"),
    EXPENSE_AMOUNT_INVALID(1302, "记账金额无效"),
    EXPENSE_PARTICIPANTS_INVALID(1303, "记账参与者无效"),
    EXPENSE_SPLIT_ERROR(1304, "分账计算错误"),

    // 邀请相关错误
    INVITATION_NOT_FOUND(1401, "邀请不存在"),
    INVITATION_EXPIRED(1402, "邀请已过期"),
    INVITATION_ALREADY_PROCESSED(1403, "邀请已处理"),
    INVITATION_SELF_INVITE(1404, "不能邀请自己"),

    // 结算相关错误
    SETTLEMENT_NOT_FOUND(1501, "结算记录不存在"),
    SETTLEMENT_ALREADY_COMPLETED(1502, "结算已完成"),
    SETTLEMENT_AMOUNT_INVALID(1503, "结算金额无效"),

    // 文件相关错误
    FILE_NOT_FOUND(1601, "文件不存在"),
    FILE_TYPE_NOT_SUPPORTED(1602, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(1603, "文件大小超出限制"),
    FILE_UPLOAD_FAILED(1604, "文件上传失败"),

    // 系统相关错误
    SYSTEM_MAINTENANCE(1701, "系统维护中"),
    RATE_LIMIT_EXCEEDED(1702, "请求频率超出限制"),
    DATA_EXPORT_FAILED(1703, "数据导出失败"),
    DATA_IMPORT_FAILED(1704, "数据导入失败");

    private final Integer code;
    private final String message;

    /**
     * 根据状态码获取枚举
     */
    public static ResponseCode getByCode(Integer code) {
        for (ResponseCode responseCode : values()) {
            if (responseCode.getCode().equals(code)) {
                return responseCode;
            }
        }
        return null;
    }

    /**
     * 根据状态码获取消息
     */
    public static String getMessageByCode(Integer code) {
        ResponseCode responseCode = getByCode(code);
        return responseCode != null ? responseCode.getMessage() : "未知错误";
    }
}
