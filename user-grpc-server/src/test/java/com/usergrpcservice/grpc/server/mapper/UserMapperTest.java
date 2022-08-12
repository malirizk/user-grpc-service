package com.usergrpcservice.grpc.server.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.ActiveProfiles;

import com.usergrpcservice.grpc.AddUserRequest;
import com.usergrpcservice.grpc.UpdateUserRequest;
import com.usergrpcservice.grpc.UserResponse;
import com.usergrpcservice.grpc.server.model.UserEntity;

@ActiveProfiles("test")
public class UserMapperTest {

	private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

	@Test
	void Should_Return_UserEntity_From_AddUserRequest() {
		AddUserRequest addUserRequest = AddUserRequest.newBuilder().setFirstName("Alice").setLastName("Bob")
				.setNickname("Ab123").setPassword("supersecurepassword").setEmail("alice@bob.com").setCountry("UK")
				.build();

		UserEntity userEntity = userMapper.toUserEntity(addUserRequest);

		assertEquals(addUserRequest.getFirstName(), userEntity.getFirstName());
		assertEquals(addUserRequest.getLastName(), userEntity.getLastName());
		assertEquals(addUserRequest.getNickname(), userEntity.getNickname());
		assertEquals(addUserRequest.getPassword(), userEntity.getPassword());
		assertEquals(addUserRequest.getEmail(), userEntity.getEmail());
		assertEquals(addUserRequest.getCountry(), userEntity.getCountry());
	}

	@Test
	void Should_Return_UserResponse_From_UserEntity() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(UUID.randomUUID());
		userEntity.setFirstName("Alice");
		userEntity.setLastName("Bob");
		userEntity.setNickname("Ab123");
		userEntity.setPassword("supersecurepassword");
		userEntity.setEmail("alice@bob.com");
		userEntity.setCountry("UK");
		userEntity.setCreatedAt(LocalDateTime.now());
		userEntity.setUpdatedAt(LocalDateTime.now());

		UserResponse userResponse = userMapper.toUserResponse(userEntity);

		assertEquals(userEntity.getId().toString(), userResponse.getId());
		assertEquals(userEntity.getFirstName(), userResponse.getFirstName());
		assertEquals(userEntity.getLastName(), userResponse.getLastName());
		assertEquals(userEntity.getNickname(), userResponse.getNickname());
		assertEquals(userEntity.getEmail(), userResponse.getEmail());
		assertEquals(userEntity.getCountry(), userResponse.getCountry());
		assertEquals(userEntity.getCreatedAt().format(DateTimeFormatter.ofPattern(UserMapper.FORMAT)),
				userResponse.getCreatedAt());
		assertEquals(userEntity.getUpdatedAt().format(DateTimeFormatter.ofPattern(UserMapper.FORMAT)),
				userResponse.getUpdatedAt());
	}

	@Test
	void Should_Update_UserEntity_From_UpdateUserRequest() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(UUID.randomUUID());
		userEntity.setFirstName("Alice");
		userEntity.setLastName("Bob");
		userEntity.setNickname("Ab123");
		userEntity.setPassword("supersecurepassword");
		userEntity.setEmail("alice@bob.com");
		userEntity.setCountry("UK");
		LocalDateTime localDateTime = LocalDateTime.now();
		userEntity.setCreatedAt(localDateTime);
		userEntity.setUpdatedAt(localDateTime);

		UUID id = UUID.randomUUID();
		UpdateUserRequest updateUserRequest = UpdateUserRequest.newBuilder().setId(id.toString())
				.setFirstName("Charlie").build();

		userMapper.updateUserEntityFromUpdateUserRequest(updateUserRequest, userEntity);

		assertEquals(id, userEntity.getId());
		assertEquals("Charlie", userEntity.getFirstName());
		assertEquals("Bob", userEntity.getLastName());
		assertEquals("Ab123", userEntity.getNickname());
		assertEquals("supersecurepassword", userEntity.getPassword());
		assertEquals("alice@bob.com", userEntity.getEmail());
		assertEquals("UK", userEntity.getCountry());
		assertEquals(localDateTime, userEntity.getCreatedAt());
		assertEquals(localDateTime, userEntity.getUpdatedAt());
	}

	@Test
	void Should_Return_UpdateUserRequest_From_UserEntity() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(UUID.randomUUID());
		userEntity.setFirstName("Alice");
		userEntity.setLastName("Bob");
		userEntity.setNickname("Ab123");
		userEntity.setPassword("supersecurepassword");
		userEntity.setEmail("alice@bob.com");
		userEntity.setCountry("UK");
		userEntity.setCreatedAt(LocalDateTime.now());
		userEntity.setUpdatedAt(LocalDateTime.now());

		UpdateUserRequest updateUserRequest = userMapper.toUpdateUserRequest(userEntity);

		assertEquals(userEntity.getId().toString(), updateUserRequest.getId());
		assertEquals(userEntity.getFirstName(), updateUserRequest.getFirstName());
		assertEquals(userEntity.getLastName(), updateUserRequest.getLastName());
		assertEquals(userEntity.getNickname(), updateUserRequest.getNickname());
		assertEquals(userEntity.getEmail(), updateUserRequest.getEmail());
		assertEquals(userEntity.getPassword(), updateUserRequest.getPassword());
		assertEquals(userEntity.getCountry(), updateUserRequest.getCountry());
	}
}
