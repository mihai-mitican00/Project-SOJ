package com.endava.tmd.bookclubproject.utilities;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface HttpResponseUtilities {

    static ResponseEntity<String> wrongParameters(){
        return new ResponseEntity<>("Parameters introduced are wrong!", HttpStatus.NOT_ACCEPTABLE);
    }

    static ResponseEntity<String> noContentFound(){
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    static ResponseEntity<String> operationWasDone(final String body){
        return ResponseEntity.ok(body);
    }

    static ResponseEntity<String> insertDone(final String body){
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    static ResponseEntity<String> dataConflict(final String body){
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    static ResponseEntity<String> notAcceptable(final String body){
        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }
}
