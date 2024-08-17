package com.ctwi.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class ResponseBase<T extends ResponseBase<T>> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ErrorResponse error;

    @SuppressWarnings("unchecked")
    public T withError(String message) {
        this.error = new ErrorResponse();
        this.error.message = message;

        return (T) this;
    }
}
