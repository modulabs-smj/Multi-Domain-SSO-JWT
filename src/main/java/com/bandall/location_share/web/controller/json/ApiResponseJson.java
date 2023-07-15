package com.bandall.location_share.web.controller.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
public class ApiResponseJson {
    public HttpStatus httpStatus;
    public Object data;
}
