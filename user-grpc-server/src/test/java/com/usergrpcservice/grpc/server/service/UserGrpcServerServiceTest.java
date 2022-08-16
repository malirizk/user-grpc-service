package com.usergrpcservice.grpc.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;

import com.usergrpcservice.grpc.*;
import com.usergrpcservice.grpc.server.exception.BusinessException;
import com.usergrpcservice.grpc.server.exception.ExceptionMap;
import com.usergrpcservice.grpc.server.mapper.UserMapper;
import com.usergrpcservice.grpc.server.model.UserEntity;
import com.usergrpcservice.grpc.server.repository.UserEntityRepository;

import io.grpc.internal.testing.StreamRecorder;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserGrpcServerServiceTest {

	private final String userId = "d2a7924e-765f-4949-bc4c-219c956d0f8b";
	private final AddUserRequest userRequest = AddUserRequest.newBuilder().setFirstName("Alice").setLastName("Bob")
			.setNickname("Ab123").setPassword("supersecurepassword").setEmail("alice@bob.com").setCountry(Country.UK)
			.build();
	private final AddUserRequest userRequest2 = AddUserRequest.newBuilder().setFirstName("Charlie").setLastName("Bob")
			.setNickname("Ab1234").setPassword("supersecurepassword").setEmail("charlie@bob.com").setCountry(Country.UK)
			.build();

	private UserGrpcServerService userGrpcServerService;
	private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
	@Mock
	private UserEntityRepository userEntityRepository;
	private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	@Mock
	private UserProducerEventService userProducerEventService;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
		userGrpcServerService = new UserGrpcServerService(userMapper, userEntityRepository, validator,
				userProducerEventService);
	}

	@Test
	void Should_Success_When_Add_User() throws Exception {
		UserEntity userEntity = userMapper.toUserEntity(userRequest);
		when(userEntityRepository.findByNickname(userEntity.getNickname())).thenReturn(Optional.empty());
		userEntity.setId(UUID.randomUUID());
		when(userEntityRepository.save(any())).thenReturn(userEntity);

		StreamRecorder<UserResponse> responseObserver = StreamRecorder.create();
		userGrpcServerService.addUser(userRequest, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail("The call did not terminate in time");
		}

		assertNull(responseObserver.getError());
		List<UserResponse> results = responseObserver.getValues();
		assertEquals(1, results.size());
		UserResponse response = results.get(0);
		assertNotNull(response);
	}

	@Test
	void Should_Throw_Exception_When_Add_Empty_User() throws Exception {
		assertThrows(ConstraintViolationException.class,
				() -> userGrpcServerService.addUser(AddUserRequest.newBuilder().build(), StreamRecorder.create()));
	}

	@Test
	void Should_Throw_Exception_When_Add_User_Is_Exist() throws Exception {
		UserEntity userEntity = userMapper.toUserEntity(userRequest);
		when(userEntityRepository.findByNickname(userEntity.getNickname())).thenReturn(Optional.of(userEntity));

		assertThrows(BusinessException.class,
				() -> userGrpcServerService.addUser(userRequest, StreamRecorder.create()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "	"})
	void Should_Throw_Exception_When_Update_User_With_Blank_Id(String id) throws Exception {
		BusinessException businessException = assertThrows(BusinessException.class, () -> userGrpcServerService
				.updateUser(UpdateUserRequest.newBuilder().setId(id).build(), StreamRecorder.create()));
		assertEquals(ExceptionMap.USER_NOT_FOUND, businessException.getDetails());
	}

	@Test
	void Should_Throw_Exception_When_Update_User_Not_Exist() throws Exception {
		when(userEntityRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
		BusinessException businessException = assertThrows(BusinessException.class, () -> userGrpcServerService
				.updateUser(UpdateUserRequest.newBuilder().setId(userId).build(), StreamRecorder.create()));
		assertEquals(ExceptionMap.USER_NOT_FOUND, businessException.getDetails());
	}

	@Test
	void Should_Throw_Exception_When_Update_User_With_Nickname_Already_Exist() throws Exception {
		UserEntity userEntity = userMapper.toUserEntity(userRequest);
		userEntity.setId(UUID.fromString(userId));
		when(userEntityRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));

		UserEntity userEntity2 = userMapper.toUserEntity(userRequest2);
		userEntity2.setId(UUID.fromString("d2a7924e-765f-4949-bc4c-219c956d0f8c"));
		when(userEntityRepository.findByNickname(userRequest2.getNickname())).thenReturn(Optional.of(userEntity2));

		BusinessException businessException = assertThrows(BusinessException.class,
				() -> userGrpcServerService.updateUser(
						UpdateUserRequest.newBuilder().setId(userId).setNickname(userRequest2.getNickname()).build(),
						StreamRecorder.create()));
		assertEquals(ExceptionMap.NICKNAME_ALREADY_EXIST, businessException.getDetails());
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "	"})
	void Should_Throw_Exception_When_Delete_Blank_UserId(String id) {
		assertThrows(IllegalArgumentException.class,
				() -> userGrpcServerService.deleteUser(DeleteRequest.newBuilder().build(), StreamRecorder.create()));

		assertThrows(IllegalArgumentException.class, () -> userGrpcServerService
				.deleteUser(DeleteRequest.newBuilder().setId(id).build(), StreamRecorder.create()));
	}

	@Test
	void Should_Throw_Exception_When_Delete_User_Invalid_UUID() {
		assertThrows(IllegalArgumentException.class, () -> userGrpcServerService
				.deleteUser(DeleteRequest.newBuilder().setId("testFakeId").build(), StreamRecorder.create()));
	}

	@Test
	void Should_Throw_Exception_When_Delete_User_Not_Exist() {
		when(userEntityRepository.findById(UUID.fromString(userId))).thenReturn(Optional.empty());

		assertThrows(BusinessException.class, () -> userGrpcServerService
				.deleteUser(DeleteRequest.newBuilder().setId(userId).build(), StreamRecorder.create()));
	}

	@Test
	void Should_Success_Exception_When_Delete_User() {
		UserEntity userEntity = userMapper.toUserEntity(userRequest);
		userEntity.setId(UUID.fromString(userId));
		when(userEntityRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));

		assertDoesNotThrow(() -> userGrpcServerService.deleteUser(DeleteRequest.newBuilder().setId(userId).build(),
				StreamRecorder.create()));
	}
}
