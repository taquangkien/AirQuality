package com.iot.airsense.config.advice;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // Bỏ qua endpoint của Swagger
        return !returnType.getContainingClass().getPackage().getName().contains("springdoc");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        HttpServletResponse servletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();

        HttpStatus originalStatus = HttpStatus.valueOf(servletResponse.getStatus());
        servletResponse.setStatus(HttpStatus.OK.value());

        // Trường hợp các API trả về các trạng thái như badRequest, notFound, forbidden, v.v
        if (body == null) {
            return new ApiResponse<>(originalStatus.value(), originalStatus.getReasonPhrase(), null);
        }

        if(body instanceof ApiResponse) return body;

        return new ApiResponse<>(HttpStatus.OK.value(), "Success", body);
    }
}
