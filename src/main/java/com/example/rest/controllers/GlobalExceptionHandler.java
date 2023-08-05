package com.example.rest.controllers;

import com.example.common.exceptions.TypeNotFoundException;
import com.example.common.utils.ErrorResponse;
import com.example.common.utils.StackTraceUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {Exception.class, TypeNotFoundException.class})
    ErrorResponse badRequest(Exception ex) {

        return ErrorResponse.builder()
                .status(INTERNAL_SERVER_ERROR.value())
                .error(ex.getMessage())
                .trace(StackTraceUtil.getStackTrace(ex))
                .timestamp(LocalDateTime.now())
                .build();
    }
}