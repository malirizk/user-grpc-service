package com.usergrpcservice.grpc.server.mapper;

import org.mapstruct.*;

import com.usergrpcservice.grpc.AddUserRequest;
import com.usergrpcservice.grpc.UpdateUserRequest;
import com.usergrpcservice.grpc.UserResponse;
import com.usergrpcservice.grpc.server.model.UserEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
	public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";

	UserEntity toUserEntity(AddUserRequest addUserRequest);

	@Mappings({@Mapping(target = "createdAt", source = "userEntity.createdAt", dateFormat = FORMAT),
			@Mapping(target = "updatedAt", source = "userEntity.updatedAt", dateFormat = FORMAT)})
	UserResponse toUserResponse(UserEntity userEntity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	void updateUserEntityFromUpdateUserRequest(UpdateUserRequest updateUserRequest,
			@MappingTarget UserEntity userEntity);

	UpdateUserRequest toUpdateUserRequest(UserEntity userEntity);
}
