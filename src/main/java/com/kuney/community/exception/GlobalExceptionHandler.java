package com.kuney.community.exception;

import com.alibaba.fastjson.JSONObject;
import com.kuney.community.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author kuneychen
 * @since 2022/6/18 15:10
 */
@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public void exception(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.error("服务器异常：{}", e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            log.error(element.toString());
        }
        String requestType = request.getHeader("x-requested-with");
        try {
            if ("XMLHttpRequest".equals(requestType)) {
                response.setContentType("application/plain;charset=utf-8");
                response.getWriter().write(JSONObject.toJSONString(Result.error("服务器异常")));
            } else {
                response.sendRedirect(request.getContextPath() + "/error");
            }
        } catch (IOException ex) {
            log.error("服务器响应异常：{}", ex.getMessage());
        }
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Result bindException(BindException e) {
        String message = e.getBindingResult().getAllErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(";"));
        return Result.fail(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Result constraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream().map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(";"));
        return Result.fail(message);
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public Result customException(CustomException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
}
