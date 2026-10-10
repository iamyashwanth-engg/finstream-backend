package com.banking.finstream.finstream_backend.exception;

public class DuplicateTranscationException extends RuntimeException{
    public DuplicateTranscationException(String message){
        super(message);
    }
}
