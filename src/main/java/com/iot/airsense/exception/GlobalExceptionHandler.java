package com.iot.airsense.exception;

import com.iot.airsense.config.advice.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Object> handleNotFoundException(NotFoundException ex) {
        logger.error("Exception occurred: " + ex);
        return new ApiResponse<>(404, ex.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("Exception occurred: " + ex);
        return new ApiResponse<>(400, "Request body is invalid", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        logger.error("Exception occurred: " + ex);
        return new ApiResponse<>(400, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleException(Exception ex) {
        logger.error("Exception occurred: " + ex);
        return new ApiResponse<>(500, "An unexpected error occurred", null);
    }
}
