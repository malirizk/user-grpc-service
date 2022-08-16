package com.usergrpcservice.grpc.server.exception;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.springframework.transaction.TransactionSystemException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

	@GrpcExceptionHandler(BusinessException.class)
	public StatusRuntimeException handleBusinessException(BusinessException e) {
		return e.getDetails().getStatus().withDescription(e.getDetails().getMessage()).withCause(e)
				.asRuntimeException();
	}

	@GrpcExceptionHandler({ValidationException.class, IllegalArgumentException.class})
	public StatusRuntimeException handleValidationException(RuntimeException e) {
		return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asRuntimeException();
	}

	@GrpcExceptionHandler(Exception.class)
	public StatusRuntimeException handleGeneralException(Exception e) {
		return Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException();
	}

	@GrpcExceptionHandler(TransactionSystemException.class)
	public StatusRuntimeException handleTransactionSystemException(TransactionSystemException e) {
		if (e.getRootCause() instanceof ConstraintViolationException) {
			return Status.INVALID_ARGUMENT
					.withDescription(((ConstraintViolationException) e.getRootCause()).getMessage()).withCause(e)
					.asRuntimeException();
		}
		return Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException();
	}
}
