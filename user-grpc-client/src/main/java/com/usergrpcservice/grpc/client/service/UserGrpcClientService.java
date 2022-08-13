package com.usergrpcservice.grpc.client.service;

import org.springframework.stereotype.Service;

import com.usergrpcservice.grpc.*;
import com.usergrpcservice.grpc.client.dto.UserRequestDto;
import com.usergrpcservice.grpc.client.dto.UserResponseDto;
import com.usergrpcservice.grpc.client.dto.UserSearchResponseDto;
import com.usergrpcservice.grpc.client.mapper.UserClientMapper;

import lombok.AllArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
@AllArgsConstructor
public class UserGrpcClientService {

	@GrpcClient("user-grpc-server")
	private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;

	private final UserClientMapper userClientMapper;

	public UserResponseDto addUser(UserRequestDto userRequestDto) {
		UserResponse userResponse = userServiceStub.addUser(userClientMapper.toAddUserRequest(userRequestDto));
		return userClientMapper.toUserResponseDto(userResponse);
	}

	public UserResponseDto updateUser(String id, UserRequestDto userRequestDto) {
		UpdateUserRequest updateUserRequest = userClientMapper.toUpdateUserRequest(id, userRequestDto);
		return userClientMapper.toUserResponseDto(userServiceStub.updateUser(updateUserRequest));
	}

	public void deleteUser(String id) {
		userServiceStub.deleteUser(DeleteRequest.newBuilder().setId(id).build());
	}

	public UserSearchResponseDto findUsers(int pageNo, int pageSize, String query) {
		SearchRequest searchRequest = SearchRequest.newBuilder().setPageNumber(pageNo).setResultPerPage(pageSize)
				.setQuery(query).build();
		SearchResponse searchResponse = userServiceStub.listUsers(searchRequest);
		return userClientMapper.toUserSearchResponseDto(searchResponse);
	}
}