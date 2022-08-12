package com.usergrpcservice.grpc.server.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends RuntimeException {

    private ExceptionMap details;

    public BusinessException(ExceptionMap exceptionMap) {
        this.details = exceptionMap;
    }
}
