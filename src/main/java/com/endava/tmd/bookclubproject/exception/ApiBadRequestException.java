package com.endava.tmd.bookclubproject.exception;

public class ApiBadRequestException extends RuntimeException{
    public ApiBadRequestException(final String message){
        super(message);
    }
}
