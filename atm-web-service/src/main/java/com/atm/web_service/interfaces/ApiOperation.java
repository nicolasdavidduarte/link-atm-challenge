package com.atm.web_service.interfaces;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface ApiOperation {
    Object execute(JsonObject json) throws Exception;
}
