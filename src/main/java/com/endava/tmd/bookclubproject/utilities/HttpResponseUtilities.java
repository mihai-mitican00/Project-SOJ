package com.endava.tmd.bookclubproject.utilities;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface HttpResponseUtilities {

    static  ResponseEntity<String> wrongParameters(){
        return new ResponseEntity<>("Parameters introduced are wrong!", HttpStatus.NOT_ACCEPTABLE);
    }

    static <T> ResponseEntity<T> noContentFound(){
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    static <T> ResponseEntity<T> operationSuccess(final T body){
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    static <T> ResponseEntity<T> insertSuccess(final T body){
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    static <T> ResponseEntity<T> dataConflict(final T body){
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    static <T> ResponseEntity<T> notAcceptable(final T body){
        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }

    static <T> ResponseEntity<T> badRequest(final T body){
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
