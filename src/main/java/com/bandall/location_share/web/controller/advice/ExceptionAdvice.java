package com.bandall.location_share.web.controller.advice;

import com.bandall.location_share.domain.exceptions.BadResponseException;
import com.bandall.location_share.domain.exceptions.EmailNotVerifiedException;
import com.bandall.location_share.domain.exceptions.IdTokenNotValidException;
import com.bandall.location_share.domain.exceptions.NoPageException;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import com.bandall.location_share.web.controller.json.ResponseStatusCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;


@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    private static final String ERROR_MSG_KEY = "errMsg";
    private static final String EMAIL_KEY = "email";

    private ApiResponseJson createApiResponse(HttpStatus status, int code, String message) {
        return new ApiResponseJson(status, code, Map.of(ERROR_MSG_KEY, message));
    }

    private ApiResponseJson createApiResponse(HttpStatus status, int code, String message, String email) {
        return new ApiResponseJson(status, code, Map.of(ERROR_MSG_KEY, message, EMAIL_KEY, email));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class,
            DataAccessException.class})
    public ApiResponseJson handleServerException(Exception e) {
        if (e instanceof DataAccessException) {
            log.info("Database 오류 발생", e);
        } else {
            log.error("서버 오류 발생", e);
        }
        return createApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseStatusCode.SERVER_ERROR, ControllerMessage.INTERNAL_SERVER_ERROR_MSG);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponseJson handleNoHandlerFoundException() {
        return createApiResponse(HttpStatus.NOT_FOUND, ResponseStatusCode.URL_NOT_FOUND, ControllerMessage.WRONG_PATH_ERROR_MSG);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class,
            IllegalStateException.class,
            NoPageException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ApiResponseJson handleBadRequestException(Exception e) {
        String message = e.getMessage();
        return createApiResponse(HttpStatus.BAD_REQUEST, ResponseStatusCode.WRONG_PARAMETER, message);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({BadCredentialsException.class,
            BadResponseException.class,
            EmailNotVerifiedException.class})
    public ApiResponseJson handleUnauthorizedException(Exception e) {
        if (e instanceof EmailNotVerifiedException) {
            return createApiResponse(HttpStatus.UNAUTHORIZED, ResponseStatusCode.EMAIL_NOT_VERIFIED, e.getMessage(), ((EmailNotVerifiedException) e).getEmail());
        }
        return createApiResponse(HttpStatus.UNAUTHORIZED, ResponseStatusCode.LOGIN_FAILED, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(IdTokenNotValidException.class)
    public ApiResponseJson handleBadIdTokenException(IdTokenNotValidException e, HttpServletResponse response) {
        Cookie idTokenCookie = new Cookie("idToken", null);
        idTokenCookie.setMaxAge(0);
        response.addCookie(idTokenCookie);
        return createApiResponse(HttpStatus.UNAUTHORIZED, ResponseStatusCode.TOKEN_VALIDATION_TRY_FAILED, e.getMessage());
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponseJson handleMethodNotAllowedException() {
        return new ApiResponseJson(HttpStatus.METHOD_NOT_ALLOWED, 0, Map.of());
    }
}
