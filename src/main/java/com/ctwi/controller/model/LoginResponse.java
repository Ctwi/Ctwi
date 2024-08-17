package com.ctwi.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class LoginResponse extends ResponseBase<LoginResponse>{
    public static LoginResponse createSuccess() {
        return new LoginResponse();
    }
}
