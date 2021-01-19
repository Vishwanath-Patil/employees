package com.springboot.employees.exceptions;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@ControllerAdvice
@RestController
public class CustomValidationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleBadGatewayException(Exception ex) {

        ErrorDetails errorDetails = new ErrorDetails(new Date(), UUID.randomUUID().toString(), ex.getMessage());

        if(ex instanceof BadRequestException){
            errorDetails.setHttpStatus(HttpStatus.BAD_REQUEST.value());
            errorDetails.setErrorType("BadRequestException");
        }

        if(ex instanceof ItemNotFoundException) {
            errorDetails.setHttpStatus(HttpStatus.NOT_FOUND.value());
            errorDetails.setErrorType("ItemNotFoundException");
        }

        if(ex instanceof UnauthorizedException) {
            errorDetails.setHttpStatus(HttpStatus.UNAUTHORIZED.value());
            errorDetails.setErrorType("UnauthorizedException");
        }

        if(ex instanceof ForbiddenException) {
            errorDetails.setHttpStatus(HttpStatus.FORBIDDEN.value());
            errorDetails.setErrorType("ForbiddenException");
        }

        if(ex instanceof DuplicateItemException) {
            errorDetails.setHttpStatus(HttpStatus.CONFLICT.value());
            errorDetails.setErrorType("DuplicateItemException");
        }

        if(ex instanceof ServiceUnavailableException) {
            errorDetails.setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            errorDetails.setErrorType("ServiceUnavailableException");
        }

        if(ex instanceof ConfigurationException) {
            errorDetails.setHttpStatus(HttpStatus.NOT_ACCEPTABLE.value());
            errorDetails.setErrorType("ConfigurationException");
        }

        if(ex instanceof DuplicateKeyException) {
            errorDetails.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorDetails.setErrorType("DuplicateKeyException");
        }

        return new ResponseEntity<>(errorDetails, HttpStatus.valueOf(errorDetails.getHttpStatus()));
    }


    @Override
    protected ResponseEntity handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        final List<String> errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }


        ErrorDetails errorDetails = new ErrorDetails(new Date(), UUID.randomUUID().toString(), errors.get(0));
        errorDetails.setHttpStatus(status.value());
        errorDetails.setErrorType("BadRequest");
        return new ResponseEntity(errorDetails, HttpStatus.valueOf(errorDetails.getHttpStatus()));
    }
}


