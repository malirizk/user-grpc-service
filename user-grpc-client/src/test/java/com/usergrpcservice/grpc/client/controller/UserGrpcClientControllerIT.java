package com.usergrpcservice.grpc.client.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.usergrpcservice.grpc.UserServiceGrpc;
import com.usergrpcservice.grpc.client.GrpcClientApplication;
import com.usergrpcservice.grpc.client.config.UserGrpcClientServiceITConfig;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usergrpcservice.grpc.client.dto.UserRequestDto;
import com.usergrpcservice.grpc.client.service.UserGrpcClientService;

@ActiveProfiles("test-client")
@SpringBootTest(classes = GrpcClientApplication.class, properties = {"grpc.server.inProcessName=test-client", "grpc.server.port=-1",
		"grpc.client.user-grpc-server.address=in-process:test-client", "spring.main.allow-bean-definition-overriding=true"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UserGrpcClientControllerIT {

	private final String V1_API_USERS = "/v1/api/users/";

	@Autowired
	private UserGrpcClientService userGrpcClientService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}

	@Ignore
	@Test
	void Should_Success_When_Add_User() throws Exception {
		UserRequestDto userRequestDto = UserRequestDto.builder().firstName("Alice").lastName("Bob").nickname("Ab123")
				.password("supersecurepassword").email("alice@bob.com").country("UK").build();

		assertNotNull(userGrpcClientService);
		mockMvc.perform(post(V1_API_USERS).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userRequestDto))).andExpect(status().isCreated())
		.andExpect(jsonPath("$.country").value("UK"));;
	}
}
