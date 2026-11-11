package com.atm.web_service.handlers;

public class ErrorHandler {

    public String handle(Exception exception) {
        return exception.getMessage();
    }
}