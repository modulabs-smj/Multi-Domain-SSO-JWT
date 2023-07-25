package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.exceptions.BadResponseException;
import com.bandall.location_share.domain.exceptions.EmailNotVerified;
import com.bandall.location_share.domain.exceptions.SocialLoginOnlyException;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.Code;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
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
        return new ApiResponseJson(HttpStatus.INTERNAL_SERVER_ERROR, Code.SERVER_ERROR, Map.of("errMsg", "서버에 오류가 발생하였습니다."));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataAccessException.class)
    public ApiResponseJson defaultError(Exception e) {
        log.error("DB 오류 발생", e);
        return new ApiResponseJson(HttpStatus.INTERNAL_SERVER_ERROR, Code.SERVER_ERROR, Map.of("errMsg", "서버에 오류가 발생하였습니다."));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponseJson pathNotFound(Exception e) {
        return new ApiResponseJson(HttpStatus.NOT_FOUND, Code.URL_NOT_FOUND, Map.of("errMsg", "존재하지 않는 경로입니다."));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponseJson badRequest(Exception e) {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, Code.WRONG_PARAMETER, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, TypeMismatchException.class, HttpMessageNotReadableException.class})
    public ApiResponseJson badRequestBody(Exception e) {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, Code.WRONG_PARAMETER, Map.of("errMsg", "잘못된 요청입니다."));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponseJson badState(Exception e) {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, Code.WRONG_PARAMETER, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponseJson badCredential(Exception e) {
        return new ApiResponseJson(HttpStatus.UNAUTHORIZED, Code.LOGIN_FAILED, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadResponseException.class)
    public ApiResponseJson badServerResponse(Exception e) {
        return new ApiResponseJson(HttpStatus.UNAUTHORIZED, Code.LOGIN_FAILED, Map.of("errMsg", e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(EmailNotVerified.class)
    public ApiResponseJson emailNotVerified(EmailNotVerified e) {
        return new ApiResponseJson(HttpStatus.UNAUTHORIZED, Code.EMAIL_NOT_VERIFIED, Map.of("errMsg", e.getMessage(), "email", e.getEmail()));
    }
}
