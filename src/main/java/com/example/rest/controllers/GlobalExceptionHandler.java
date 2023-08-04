package com.example.rest.controllers;

import com.example.common.utils.ErrorResponse;
import com.example.common.utils.StackTraceUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {RuntimeException.class})
    ErrorResponse badRequest(RuntimeException ex) {

        return ErrorResponse.builder()
                .status(BAD_REQUEST.value())
                .error(ex.getMessage())
                .trace(StackTraceUtil.getStackTrace(ex))
                .timestamp(LocalDateTime.now())
                .build();
    }
}