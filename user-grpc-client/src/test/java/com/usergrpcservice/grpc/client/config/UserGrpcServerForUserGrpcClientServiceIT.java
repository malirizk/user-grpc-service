package com.usergrpcservice.grpc.client.config;

import com.usergrpcservice.grpc.AddUserRequest;
import com.usergrpcservice.grpc.UserResponse;
import com.usergrpcservice.grpc.UserServiceGrpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test-client")
@GrpcService
public class UserGrpcServerForUserGrpcClientServiceIT extends UserServiceGrpc.UserServiceImplBase {

	@Override
	public void addUser(AddUserRequest request, StreamObserver<UserResponse> responseObserver) {
		//responseObserver.onNext(UserResponse.getDefaultInstance());
		responseObserver.onNext(UserResponse.newBuilder().setCountry("UK").build());
		responseObserver.onCompleted();
	}
}
