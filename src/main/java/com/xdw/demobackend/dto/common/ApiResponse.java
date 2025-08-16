package com.xdw.demobackend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xdw.demobackend.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应格式
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 请求追踪ID（用于日志追踪）
     */
    private String traceId;

    /**
     * 额外的元数据信息
     */
    private Object meta;

    // ============ 静态工厂方法 ============

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .build();
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(message)
                .build();
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 业务异常响应
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.BUSINESS_ERROR.getCode())
                .message(message)
                .build();
    }

    /**
     * 参数验证失败响应
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.VALIDATION_ERROR.getCode())
                .message(message)
                .build();
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized() {
        return ApiResponse.<T>builder()
                .code(ResponseCode.UNAUTHORIZED.getCode())
                .message(ResponseCode.UNAUTHORIZED.getMessage())
                .build();
    }

    /**
     * 权限不足响应
     */
    public static <T> ApiResponse<T> forbidden() {
        return ApiResponse.<T>builder()
                .code(ResponseCode.FORBIDDEN.getCode())
                .message(ResponseCode.FORBIDDEN.getMessage())
                .build();
    }

    /**
     * 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound() {
        return ApiResponse.<T>builder()
                .code(ResponseCode.NOT_FOUND.getCode())
                .message(ResponseCode.NOT_FOUND.getMessage())
                .build();
    }

    /**
     * 资源不存在响应（自定义消息）
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.NOT_FOUND.getCode())
                .message(message)
                .build();
    }

    /**
     * 服务器内部错误响应
     */
    public static <T> ApiResponse<T> internalError() {
        return ApiResponse.<T>builder()
                .code(ResponseCode.INTERNAL_ERROR.getCode())
                .message(ResponseCode.INTERNAL_ERROR.getMessage())
                .build();
    }

    /**
     * 服务器内部错误响应（自定义消息）
     */
    public static <T> ApiResponse<T> internalError(String message) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.INTERNAL_ERROR.getCode())
                .message(message)
                .build();
    }

    // ============ 链式调用方法 ============

    /**
     * 添加追踪ID
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 添加元数据
     */
    public ApiResponse<T> withMeta(Object meta) {
        this.meta = meta;
        return this;
    }

    /**
     * 添加时间戳
     */
    public ApiResponse<T> withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    // ============ 判断方法 ============

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResponseCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}
