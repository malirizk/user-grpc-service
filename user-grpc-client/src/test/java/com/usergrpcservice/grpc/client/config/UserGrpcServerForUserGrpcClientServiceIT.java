package com.usergrpcservice.grpc.client.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import com.usergrpcservice.grpc.*;
import com.usergrpcservice.grpc.client.dto.UserResponseDto;
import com.usergrpcservice.grpc.client.mapper.UserClientMapper;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@ActiveProfiles("test")
@GrpcService
public class UserGrpcServerForUserGrpcClientServiceIT extends UserServiceGrpc.UserServiceImplBase {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private UserClientMapper userClientMapper;

	@Value("classpath:/data/addUserResponse.json")
	private Resource addUserResponse;

	@Override
	public void addUser(AddUserRequest request, StreamObserver<UserResponse> responseObserver) {
		UserResponse userResponse;
		try {
			UserResponseDto userResponseDto = objectMapper.readValue(addUserResponse.getURI().toURL(),
					UserResponseDto.class);
			userResponse = userClientMapper.toUserResponse(userResponseDto);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		responseObserver.onNext(userResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
		UserResponseDto userResponseDto = null;
		try {
			userResponseDto = objectMapper.readValue(addUserResponse.getURI().toURL(), UserResponseDto.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		userClientMapper.updateUserResponseDtoFromUpdateUserRequest(request, userResponseDto);

		responseObserver.onNext(userClientMapper.toUserResponse(userResponseDto));
		responseObserver.onCompleted();
	}

	@Override
	public void deleteUser(DeleteRequest request, StreamObserver<Empty> responseObserver) {
		responseObserver.onNext(Empty.getDefaultInstance());
		responseObserver.onCompleted();
	}
}
