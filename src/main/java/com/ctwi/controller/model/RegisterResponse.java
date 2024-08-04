package com.ctwi.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class RegisterResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ErrorResponse error;

    public static RegisterResponse createError (String message) {
        var r = new RegisterResponse();
        r.error = new ErrorResponse();
        r.error.message = message;

        return r;
    }

    public static RegisterResponse createSuccess () {
       return new RegisterResponse();
    }
}
