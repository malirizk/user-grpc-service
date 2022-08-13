package com.usergrpcservice.grpc.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.usergrpcservice.grpc.AddUserRequest;
import com.usergrpcservice.grpc.SearchResponse;
import com.usergrpcservice.grpc.UpdateUserRequest;
import com.usergrpcservice.grpc.UserResponse;
import com.usergrpcservice.grpc.client.dto.UserRequestDto;
import com.usergrpcservice.grpc.client.dto.UserResponseDto;
import com.usergrpcservice.grpc.client.dto.UserSearchResponseDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserClientMapper {

	AddUserRequest toAddUserRequest(UserRequestDto userRequestDto);

	UserResponseDto toUserResponseDto(UserResponse userResponse);

	UpdateUserRequest toUpdateUserRequest(UserRequestDto userRequestDto);

	UserSearchResponseDto toUserSearchResponseDto(SearchResponse searchResponse);

	UserResponse toUserResponse(UserResponseDto userResponseDto);
}
