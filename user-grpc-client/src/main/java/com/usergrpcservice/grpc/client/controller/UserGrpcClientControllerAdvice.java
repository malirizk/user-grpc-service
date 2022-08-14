package com.usergrpcservice.grpc.client.controller;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
class UserGrpcClientControllerAdvice {

	@ExceptionHandler(RuntimeException.class)
	ResponseEntity handleGeneralException(RuntimeException ex) throws RuntimeException {
		Status status = Status.fromThrowable(ex);
		log.error("code=" + status.getCode());
		log.error("description=" + status.getDescription());
		log.error("cause=" + status.getCause());
		if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
			throw ex;
		} else {
			if (ex instanceof NullPointerException) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
