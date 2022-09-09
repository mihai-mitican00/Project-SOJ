package com.endava.tmd.bookclubproject.exception;

public class ApiForbiddenException extends RuntimeException{
    public ApiForbiddenException() {
        super();
    }

    public ApiForbiddenException(String message) {
        super(message);
    }
}
