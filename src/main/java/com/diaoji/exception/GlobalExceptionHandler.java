package com.diaoji.exception;

import com.diaoji.vo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败（如鱼种为空、重量非法） */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    /** 无权限操作 */
    @ExceptionHandler(SecurityException.class)
    public Result<?> handleSecurityException(SecurityException e) {
        return Result.error(403, e.getMessage());
    }

    /** 数据库约束违反（如 weight NOT NULL 等） */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrity(DataIntegrityViolationException e) {
        return Result.error(400, "数据格式不合法，请检查输入");
    }

    /** 其他未预期异常 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleGeneral(Exception e) {
        e.printStackTrace();
        return Result.error(500, "服务器内部错误：" + e.getMessage());
    }
}