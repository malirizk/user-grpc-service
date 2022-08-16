package com.usergrpcservice.grpc.client.controller;

import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.usergrpcservice.grpc.client.dto.ErrorResponseDto;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
class UserGrpcClientControllerAdvice {

	@ExceptionHandler(RuntimeException.class)
	ResponseEntity handleGeneralException(RuntimeException ex) throws RuntimeException {
		Status status = Status.fromThrowable(ex);
		if (!Objects.isNull(status)) {
			ErrorResponseDto responseDto = ErrorResponseDto.builder()
					.code(Optional.ofNullable(status.getCode()).map(Status.Code::name).orElse(""))
					.cause(Optional.ofNullable(status.getCause()).map(Throwable::getMessage).orElse(""))
					.message(status.getDescription()).build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
