package com.usergrpcservice.grpc.client.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usergrpcservice.grpc.AddUserRequest;
import com.usergrpcservice.grpc.UserResponse;
import com.usergrpcservice.grpc.UserServiceGrpc;
import com.usergrpcservice.grpc.client.config.UserGrpcClientServiceITConfig;
import com.usergrpcservice.grpc.client.dto.UserRequestDto;
import com.usergrpcservice.grpc.client.dto.UserResponseDto;
import com.usergrpcservice.grpc.client.mapper.UserClientMapper;
import com.usergrpcservice.grpc.client.service.UserGrpcClientService;

import net.devh.boot.grpc.client.inject.GrpcClient;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableAutoConfiguration
@SpringBootTest(classes = {UserGrpcClientController.class, UserGrpcClientControllerAdvice.class})
@SpringJUnitConfig(classes = UserGrpcClientServiceITConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserGrpcClientControllerIT {

	private final String V1_API_USERS = "/v1/api/users/";

	@GrpcClient("user-grpc-server")
	private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

	@Autowired
	private UserGrpcClientService userGrpcClientService;

	@Autowired
	private UserClientMapper userClientMapper;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Value("classpath:/data/addUserRequest.json")
	private Resource addUserRequest;
	@Value("classpath:/data/addUserResponse.json")
	private Resource addUserResponse;

	@BeforeEach
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}

	@Test
	void Should_Return_When_Add_UserFrom_Stub() throws Exception {
		UserRequestDto userRequestDto = objectMapper.readValue(addUserRequest.getURI().toURL(), UserRequestDto.class);
		AddUserRequest addUserRequest = userClientMapper.toAddUserRequest(userRequestDto);
		UserResponse actualUserResponse = userServiceStub.addUser(addUserRequest);

		UserResponseDto userResponseDto = objectMapper.readValue(addUserResponse.getURI().toURL(),
				UserResponseDto.class);
		UserResponse expectedUserResponse = userClientMapper.toUserResponse(userResponseDto);

		assertNotNull(actualUserResponse);
		assertEquals(expectedUserResponse, actualUserResponse);
	}

	@Test
	void Should_Return_When_Add_User_From_Service() throws Exception {
		UserRequestDto userRequestDto = objectMapper.readValue(addUserRequest.getURI().toURL(), UserRequestDto.class);
		UserResponseDto actualUserResponseDto = userGrpcClientService.addUser(userRequestDto);

		UserResponseDto expectedUserResponseDto = objectMapper.readValue(addUserResponse.getURI().toURL(),
				UserResponseDto.class);

		assertNotNull(expectedUserResponseDto);
		assertEquals(expectedUserResponseDto, actualUserResponseDto);
	}
	@Test
	void Should_Success_When_Add_User() throws Exception {
		mockMvc.perform(post(V1_API_USERS).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(StreamUtils.copyToString(addUserRequest.getURI().toURL().openStream(),
						Charset.defaultCharset())))
				.andDo(print()).andExpect(status().isCreated()).andExpect(content().json(StreamUtils
						.copyToString(addUserResponse.getURI().toURL().openStream(), Charset.defaultCharset())));
	}

	@Test
	void Should_Success_When_Update_User() throws Exception {
		UserResponseDto userResponseDto = objectMapper.readValue(addUserResponse.getURI().toURL(),
				UserResponseDto.class);
		assertNotNull(userResponseDto);
		String newFirstName = "Charlie";
		String newCountry = "PL";
		String updateUserRequestJson = "{ \"first_name\" : \"" + newFirstName + "\", \"country\" : \"" + newCountry
				+ "\" }";
		userResponseDto.setFirstName(newFirstName);
		userResponseDto.setCountry(newCountry);

		mockMvc.perform(put(V1_API_USERS + userResponseDto.getId()).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(updateUserRequestJson)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().json(objectMapper.writeValueAsString(userResponseDto)));
	}

	@Test
	void Should_Fail_When_Update_User_Not_Exist() throws Exception {
		mockMvc.perform(put(V1_API_USERS + "12345").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("{ \"first_name\" : \"Charlie\" }")).andDo(print())
				.andExpect(status().isInternalServerError());
	}

	@Test
	void Should_Success_When_Delete_User() throws Exception {
		UserResponseDto userResponseDto = objectMapper.readValue(addUserResponse.getURI().toURL(),
				UserResponseDto.class);
		assertNotNull(userResponseDto);

		mockMvc.perform(delete(V1_API_USERS + userResponseDto.getId()).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());
	}
}
