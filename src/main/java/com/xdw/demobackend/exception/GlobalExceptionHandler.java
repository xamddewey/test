package com.xdw.demobackend.exception;

import com.xdw.demobackend.dto.common.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ApiResult.error(400, "参数验证失败: " + errorMessage);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<?> handleBindException(BindException ex) {
        String errorMessage = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ApiResult.error(400, "参数绑定失败: " + errorMessage);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResult<?> handleBadCredentialsException(BadCredentialsException ex) {
        return ApiResult.error(401, "用户名或密码错误");
    }

    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ApiResult<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ApiResult.error(404, ex.getMessage());
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResult<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ApiResult.error(403, "权限不足: " + ex.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResult<?> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred", ex);
        return ApiResult.error(500, ex.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        return ApiResult.error(500, "系统内部错误");
    }
}
