package com.endava.tmd.bookclubproject.exception;

import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiBadRequestException.class})
    public ResponseEntity<Object> handleApiBadRequestException(final ApiBadRequestException exception) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                exception.getMessage(),
                badRequest,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(apiException, badRequest);
    }

    @ExceptionHandler(value = {ApiNotFoundException.class})
    public ResponseEntity<Object> handleApiNotFoundException(final ApiNotFoundException exception) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ApiException apiException = new ApiException(
                exception.getMessage(),
                notFound,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(apiException, notFound);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleApiForbiddenException(final ApiForbiddenException exception) {
        HttpStatus forbidden = HttpStatus.FORBIDDEN;
        ApiException apiException = new ApiException(
                exception.getMessage(),
                forbidden,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(apiException, forbidden);
    }
}
