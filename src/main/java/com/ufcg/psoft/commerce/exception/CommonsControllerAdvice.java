package com.ufcg.psoft.commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonsControllerAdvice {


    @ExceptionHandler(PlanoInvalidoException.class)
    public ResponseEntity<CustomErrorType> handlePlanoInvalido(PlanoInvalidoException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorType(e));
    }
}