package com.bandall.location_share.web.controller.advice;

import com.bandall.location_share.domain.exceptions.BadResponseException;
import com.bandall.location_share.domain.exceptions.EmailNotVerifiedException;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.TokenStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ApiResponseJson defaultError(RuntimeException e) {
        log.error("", e);
        return new ApiResponseJson(HttpStatus.INTERNAL_SERVER_ERROR, TokenStatusCode.SERVER_ERROR, Map.of("errMsg", "서버에 오류가 발생하였습니다."));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataAccessException.class)
    public ApiResponseJson defaultError(Exception e) {
        log.error("DB 오류 발생", e);
        return new ApiResponseJson(HttpStatus.INTERNAL_SERVER_ERROR, TokenStatusCode.SERVER_ERROR, Map.of("errMsg", "서버에 오류가 발생하였습니다."));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponseJson pathNotFound(Exception e) {
        return new ApiResponseJson(HttpStatus.NOT_FOUND, TokenStatusCode.URL_NOT_FOUND, Map.of("errMsg", "존재하지 않는 경로입니다."));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponseJson badRequest(Exception e) {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, TokenStatusCode.WRONG_PARAMETER, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, TypeMismatchException.class, HttpMessageNotReadableException.class})
    public ApiResponseJson badRequestBody(Exception e) {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, TokenStatusCode.WRONG_PARAMETER, Map.of("errMsg", "잘못된 요청입니다."));
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponseJson methodNotAllowed() {
        return new ApiResponseJson(HttpStatus.METHOD_NOT_ALLOWED, 0, Map.of());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponseJson badState(Exception e) {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, TokenStatusCode.WRONG_PARAMETER, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponseJson badCredential(Exception e) {
        return new ApiResponseJson(HttpStatus.UNAUTHORIZED, TokenStatusCode.LOGIN_FAILED, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadResponseException.class)
    public ApiResponseJson badServerResponse(Exception e) {
        return new ApiResponseJson(HttpStatus.UNAUTHORIZED, TokenStatusCode.LOGIN_FAILED, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ApiResponseJson emailNotVerified(EmailNotVerifiedException e) {
        return new ApiResponseJson(HttpStatus.UNAUTHORIZED, TokenStatusCode.EMAIL_NOT_VERIFIED, Map.of("errMsg", e.getMessage(), "email", e.getEmail()));
    }
}
