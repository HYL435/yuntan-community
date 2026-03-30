package com.yuntan.common.hanlder;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.yuntan.common.domain.Result;
import com.yuntan.common.exception.BusinessException;

/**
 * 全局异常处理器（MVC版）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（核心）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e,
                                                                HttpServletRequest request) {

        log.warn("业务异常，请求路径: {}, code: {}, message: {}",
                request.getRequestURI(), e.getCode(), e.getMessage(), e);

        return ResponseEntity
                .status(e.getHttpStatus()) // ⭐ 核心：用你定义的 httpStatus
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e,
                                                        HttpServletRequest request) {

        log.error("系统异常，请求路径: {}", request.getRequestURI(), e);

        return ResponseEntity
                .status(500)
                .body(Result.error(50000, "服务器内部错误"));
    }
}