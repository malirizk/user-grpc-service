package com.usergrpcservice.grpc.server.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.usergrpcservice.grpc.*;
import com.usergrpcservice.grpc.server.exception.BusinessException;
import com.usergrpcservice.grpc.server.exception.ExceptionMap;
import com.usergrpcservice.grpc.server.mapper.UserMapper;
import com.usergrpcservice.grpc.server.model.UserEntity;
import com.usergrpcservice.grpc.server.repository.UserEntityRepository;

import io.grpc.internal.testing.StreamRecorder;

@ActiveProfiles("test")
@SpringBootTest(properties = {"grpc.server.inProcessName=test", "grpc.server.port=-1",
		"grpc.client.inProcess.address=in-process:test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserGrpcServerServiceIT {

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
	private final AddUserRequest addUserRequest = AddUserRequest.newBuilder().setFirstName("Alice").setLastName("Bob")
			.setNickname("Ab123").setPassword("supersecurepassword").setEmail("alice@bob.com").setCountry("UK").build();
	private final AddUserRequest addUserRequest2 = AddUserRequest.newBuilder().setFirstName("Charlie")
			.setLastName("Bob").setNickname("Ab1234").setPassword("supersecurepassword").setEmail("charlie@bob.com")
			.setCountry("UK").build();
	private final String userId = "d2a7924e-765f-4949-bc4c-219c956d0f8b";

	@Autowired
	private UserGrpcServerService userGrpcServerService;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserEntityRepository userEntityRepository;

	@Test
	void Should_Success_When_Add_User() throws Exception {
		StreamRecorder<UserResponse> responseObserver = StreamRecorder.create();
		userGrpcServerService.addUser(addUserRequest, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail("The call did not terminate in time");
		}

		assertNull(responseObserver.getError());
		List<UserResponse> results = responseObserver.getValues();
		assertEquals(1, results.size());
		UserResponse response = results.get(0);
		assertNotNull(response.getId());
		assertEquals(addUserRequest.getFirstName(), response.getFirstName());
		assertEquals(addUserRequest.getLastName(), response.getLastName());
		assertEquals(addUserRequest.getNickname(), response.getNickname());
		assertEquals(addUserRequest.getEmail(), response.getEmail());
		assertEquals(addUserRequest.getCountry(), response.getCountry());
		assertNotNull(response.getCreatedAt());
		assertNotNull(response.getUpdatedAt());
	}

	@Test
	void Should_Throw_Exception_When_Add_User_Is_Exist() throws Exception {
		UserEntity userEntity = userMapper.toUserEntity(addUserRequest);
		userEntityRepository.save(userEntity);

		StreamRecorder<UserResponse> responseObserver = StreamRecorder.create();
		assertThrows(BusinessException.class, () -> userGrpcServerService.addUser(addUserRequest, responseObserver));
	}

	@Test
	void Should_Success_When_Update_User() throws Exception {
		UserEntity userEntity = userMapper.toUserEntity(addUserRequest);
		userEntityRepository.save(userEntity);

		UpdateUserRequest updateUserRequest = UpdateUserRequest.newBuilder().setId(userEntity.getId().toString())
				.setLastName("Bobbb").setCountry("PL").buildPartial();
		StreamRecorder<UserResponse> responseObserver = StreamRecorder.create();
		userGrpcServerService.updateUser(updateUserRequest, responseObserver);

		assertNull(responseObserver.getError());
		List<UserResponse> results = responseObserver.getValues();
		assertEquals(1, results.size());
		UserResponse response = results.get(0);
		assertEquals(updateUserRequest.getId(), response.getId());
		assertEquals(userEntity.getFirstName(), response.getFirstName());
		assertEquals(updateUserRequest.getLastName(), response.getLastName());
		assertEquals(userEntity.getNickname(), response.getNickname());
		assertEquals(userEntity.getEmail(), response.getEmail());
		assertEquals(updateUserRequest.getCountry(), response.getCountry());
		assertEquals(userEntity.getCreatedAt().format(formatter), response.getCreatedAt());
		assertEquals(userEntity.getUpdatedAt().format(formatter), response.getUpdatedAt());
	}

	@Test
	void Should_Throw_Exception_When_Update_User_Not_Exist() throws Exception {
		BusinessException businessException = assertThrows(BusinessException.class, () -> userGrpcServerService
				.updateUser(UpdateUserRequest.newBuilder().setId(userId).build(), StreamRecorder.create()));
		assertEquals(ExceptionMap.USER_NOT_FOUND, businessException.getDetails());
	}

	@Test
	void Should_Throw_Exception_When_Update_User_With_Nickname_Already_Exist() throws Exception {
		UserEntity oldUserEntity = userMapper.toUserEntity(addUserRequest);
		userEntityRepository.save(oldUserEntity);
		UserEntity newUserEntity = userMapper.toUserEntity(addUserRequest2);
		userEntityRepository.save(newUserEntity);
		newUserEntity.setNickname(oldUserEntity.getNickname());

		StreamRecorder<UserResponse> responseObserver = StreamRecorder.create();
		BusinessException businessException = assertThrows(BusinessException.class, () -> userGrpcServerService
				.updateUser(userMapper.toUpdateUserRequest(newUserEntity), responseObserver));
		assertEquals(ExceptionMap.NICKNAME_ALREADY_EXIST, businessException.getDetails());
	}

	@Test
	void Should_Throw_Exception_When_Delete_User_Not_Exist() {
		assertThrows(BusinessException.class, () -> userGrpcServerService
				.deleteUser(DeleteRequest.newBuilder().setId(userId).build(), StreamRecorder.create()));
	}

	@Test
	void Should_Success_Exception_When_Delete_User() {
		UserEntity userEntity = userMapper.toUserEntity(addUserRequest);
		userEntityRepository.save(userEntity);

		assertDoesNotThrow(() -> userGrpcServerService.deleteUser(
				DeleteRequest.newBuilder().setId(userEntity.getId().toString()).build(), StreamRecorder.create()));
	}

	@Test
	void Should_Return_Data_When_Search_User_By_Country() {
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest));
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest2));
		UserEntity userEntity3 = userMapper.toUserEntity(addUserRequest);
		userEntity3.setNickname("ABC12345");
		userEntityRepository.save(userEntity3);

		SearchRequest searchRequest = SearchRequest.newBuilder().setQuery("country=UK").setResultPerPage(3).build();
		StreamRecorder<SearchResponse> responseObserver = StreamRecorder.create();

		userGrpcServerService.listUsers(searchRequest, responseObserver);
		assertNull(responseObserver.getError());
		SearchResponse response = responseObserver.getValues().get(0);
		assertNotNull(response);
		assertEquals(1, response.getTotalPages());
		assertEquals(0, response.getPageNumber());
		assertEquals(3, response.getTotalResult());
		assertEquals(3, response.getContentList().size());
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 1})
	void Should_Return_Data_When_Search_User_Contains_FirstName(int pageNo) {
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest));
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest2));
		UserEntity userEntity3 = userMapper.toUserEntity(addUserRequest2);
		userEntity3.setNickname("ABC12345");
		userEntityRepository.save(userEntity3);

		SearchRequest searchRequest = SearchRequest.newBuilder().setQuery("first_name=arli").setPageNumber(pageNo)
				.setResultPerPage(1).build();
		StreamRecorder<SearchResponse> responseObserver = StreamRecorder.create();

		userGrpcServerService.listUsers(searchRequest, responseObserver);
		assertNull(responseObserver.getError());
		SearchResponse response = responseObserver.getValues().get(0);
		assertNotNull(response);
		assertEquals(2, response.getTotalPages());
		assertEquals(pageNo, response.getPageNumber());
		assertEquals(2, response.getTotalResult());
		assertEquals(1, response.getContentList().size());
	}

	@Test
	void Should_Return_All_When_Search_User_Without_Query() {
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest));
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest2));
		UserEntity userEntity3 = userMapper.toUserEntity(addUserRequest);
		userEntity3.setNickname("ABC12345");
		userEntityRepository.save(userEntity3);

		SearchRequest searchRequest = SearchRequest.newBuilder().setResultPerPage(3).build();
		StreamRecorder<SearchResponse> responseObserver = StreamRecorder.create();

		userGrpcServerService.listUsers(searchRequest, responseObserver);
		assertNull(responseObserver.getError());
		SearchResponse response = responseObserver.getValues().get(0);
		assertNotNull(response);
		assertEquals(1, response.getTotalPages());
		assertEquals(0, response.getPageNumber());
		assertEquals(3, response.getTotalResult());
		assertEquals(3, response.getContentList().size());
	}

	@Test
	void Should_Return_Only_One_When_Search_User_With_Firstname_And_Country() {
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest));
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest2));
		UserEntity userEntity3 = userMapper.toUserEntity(addUserRequest);
		userEntity3.setNickname("ABC12345");
		userEntityRepository.save(userEntity3);

		SearchRequest searchRequest = SearchRequest.newBuilder().setQuery("first_name=charlie&last_name=bob&country=uk")
				.setResultPerPage(10).build();
		StreamRecorder<SearchResponse> responseObserver = StreamRecorder.create();

		userGrpcServerService.listUsers(searchRequest, responseObserver);
		assertNull(responseObserver.getError());
		SearchResponse response = responseObserver.getValues().get(0);
		assertNotNull(response);
		assertEquals(1, response.getTotalPages());
		assertEquals(0, response.getPageNumber());
		assertEquals(1, response.getTotalResult());
		assertEquals(1, response.getContentList().size());
	}

	@Test
	void Should_Return_Empty_When_Search_User_With_Missing_Data() {
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest));
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest2));
		UserEntity userEntity3 = userMapper.toUserEntity(addUserRequest);
		userEntity3.setNickname("ABC12345");
		userEntityRepository.save(userEntity3);

		SearchRequest searchRequest = SearchRequest.newBuilder()
				.setQuery("first_name=charliezz&last_name=bob&country=uk").setResultPerPage(10).build();
		StreamRecorder<SearchResponse> responseObserver = StreamRecorder.create();

		userGrpcServerService.listUsers(searchRequest, responseObserver);
		assertNull(responseObserver.getError());
		SearchResponse response = responseObserver.getValues().get(0);
		assertNotNull(response);
		assertEquals(0, response.getTotalPages());
		assertEquals(0, response.getPageNumber());
		assertEquals(0, response.getTotalResult());
		assertEquals(0, response.getContentList().size());
	}

	@Test
	void Should_Return_All_When_Search_User_With_Wrong_Key() {
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest));
		userEntityRepository.save(userMapper.toUserEntity(addUserRequest2));
		UserEntity userEntity3 = userMapper.toUserEntity(addUserRequest);
		userEntity3.setNickname("ABC12345");
		userEntityRepository.save(userEntity3);

		SearchRequest searchRequest = SearchRequest.newBuilder().setQuery("firstname=charlie&lastname=bob")
				.setResultPerPage(10).build();
		StreamRecorder<SearchResponse> responseObserver = StreamRecorder.create();

		userGrpcServerService.listUsers(searchRequest, responseObserver);
		assertNull(responseObserver.getError());
		SearchResponse response = responseObserver.getValues().get(0);
		assertNotNull(response);
		assertEquals(1, response.getTotalPages());
		assertEquals(0, response.getPageNumber());
		assertEquals(3, response.getTotalResult());
		assertEquals(3, response.getContentList().size());
	}
}