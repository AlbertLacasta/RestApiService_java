package com.sample.sampleapiservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class MkAuthException extends  RuntimeException{

    public MkAuthException(String message) {
        super(message);
    }
}
