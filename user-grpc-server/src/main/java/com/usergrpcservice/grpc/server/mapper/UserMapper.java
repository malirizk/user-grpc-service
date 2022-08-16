package com.usergrpcservice.grpc.server.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.mapstruct.*;

import com.google.protobuf.Timestamp;
import com.usergrpcservice.grpc.AddUserRequest;
import com.usergrpcservice.grpc.UpdateUserRequest;
import com.usergrpcservice.grpc.UserResponse;
import com.usergrpcservice.grpc.server.model.UserEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

	UserEntity toUserEntity(AddUserRequest addUserRequest);

	UserResponse toUserResponse(UserEntity userEntity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	void updateUserEntityFromUpdateUserRequest(UpdateUserRequest updateUserRequest,
			@MappingTarget UserEntity userEntity);

	UpdateUserRequest toUpdateUserRequest(UserEntity userEntity);

	default Timestamp toProtobufTimestamp(LocalDateTime localDateTime) {
		if (localDateTime == null)
			return Timestamp.getDefaultInstance();
		Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
		return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
	}
}
