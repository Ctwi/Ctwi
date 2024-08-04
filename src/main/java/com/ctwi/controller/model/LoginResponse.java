package com.ctwi.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class LoginResponse extends RegisterResponse{
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ErrorResponse error;

    public static LoginResponse loginError (String message){
        var r = new LoginResponse();
        r.error = new ErrorResponse();
        r.error.message = message;

        return r;
    }

    public static LoginResponse loginSuccess() {
        return new LoginResponse();
    }
}
