package com.usergrpcservice.grpc.client.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.mapstruct.*;

import com.google.protobuf.Timestamp;
import com.usergrpcservice.grpc.AddUserRequest;
import com.usergrpcservice.grpc.SearchResponse;
import com.usergrpcservice.grpc.UpdateUserRequest;
import com.usergrpcservice.grpc.UserResponse;
import com.usergrpcservice.grpc.client.dto.UserRequestDto;
import com.usergrpcservice.grpc.client.dto.UserResponseDto;
import com.usergrpcservice.grpc.client.dto.UserSearchResponseDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserClientMapper {

	public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";

	AddUserRequest toAddUserRequest(UserRequestDto userRequestDto);

	UserResponseDto toUserResponseDto(UserResponse userResponse);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	UpdateUserRequest toUpdateUserRequest(String id, UserRequestDto userRequestDto);

	@Mapping(source = "contentList", target = "content")
	UserSearchResponseDto toUserSearchResponseDto(SearchResponse searchResponse);

	UserResponse toUserResponse(UserResponseDto userResponseDto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	void updateUserResponseDtoFromUpdateUserRequest(UpdateUserRequest updateUserRequest,
			@MappingTarget UserResponseDto userResponseDto);

	default Timestamp toProtobufTimestamp(String timestamp) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT);
		LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);
		Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
		return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
	}

	default String toDateTimeString(Timestamp timestamp) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT).withZone(ZoneId.from(ZoneOffset.UTC));
		Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
		return formatter.format(instant);
	}
}
