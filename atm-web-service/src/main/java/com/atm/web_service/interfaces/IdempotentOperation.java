package com.atm.web_service.interfaces;

@FunctionalInterface
public interface IdempotentOperation {
    Object execute() throws Exception;
}
