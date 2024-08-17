package com.ctwi.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class RegisterResponse extends ResponseBase<RegisterResponse> {
    public static RegisterResponse createSuccess () {
       return new RegisterResponse();
    }
}
