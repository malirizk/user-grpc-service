package com.usergrpcservice.grpc.server.exception;

import io.grpc.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMap {

    NICKNAME_ALREADY_EXIST("VAL001", "Nickname is already exist", Status.ALREADY_EXISTS),
    USER_NOT_FOUND("BUS001", "User is not found", Status.NOT_FOUND);

    private String code;
    private String message;
    private Status status;
}
