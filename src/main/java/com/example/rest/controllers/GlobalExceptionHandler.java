package com.example.rest.controllers;

import com.example.common.exceptions.ActionNotFoundException;
import com.example.common.exceptions.TypeNotFoundException;
import com.example.common.utils.ErrorResponse;
import com.example.common.utils.StackTraceUtil;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {Exception.class, TypeNotFoundException.class})
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    ErrorResponse internalError(Exception ex) {

        return ErrorResponse.builder()
                .status(INTERNAL_SERVER_ERROR.value())
                .error(ex.getMessage())
                .trace(StackTraceUtil.getStackTrace(ex))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(value = {ActionNotFoundException.class, JobExecutionException.class})
    @ResponseStatus(value = BAD_REQUEST)
    ErrorResponse badRequest(Exception ex) {

        return ErrorResponse.builder()
                .status(BAD_REQUEST.value())
                .error(ex.getMessage())
                .trace(StackTraceUtil.getStackTrace(ex))
                .timestamp(LocalDateTime.now())
                .build();
    }
}