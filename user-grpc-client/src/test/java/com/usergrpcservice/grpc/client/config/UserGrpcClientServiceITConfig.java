package com.usergrpcservice.grpc.client.config;

import com.usergrpcservice.grpc.UserServiceGrpc;
import com.usergrpcservice.grpc.client.service.UserGrpcClientService;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.usergrpcservice.grpc.client.mapper.UserClientMapper;

import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

@Profile("test-client")
@Configuration
@ImportAutoConfiguration({GrpcServerAutoConfiguration.class, GrpcServerFactoryAutoConfiguration.class,
		GrpcClientAutoConfiguration.class})
public class UserGrpcClientServiceITConfig {


    /*@Bean
	UserClientMapper userClientMapper() {
		return Mappers.getMapper(UserClientMapper.class);
	}*/


	@Bean
	@Primary
	UserGrpcClientService userGrpcClientService(){
		return new UserGrpcClientService(Mockito.mock(UserServiceGrpc.UserServiceBlockingStub.class),Mappers.getMapper(UserClientMapper.class));
		//return new UserGrpcClientService();
	}

	/*@Bean//("userGrpcServer")
	UserGrpcServerForUserGrpcClientServiceIT userGrpcServerImpl() {
		return new UserGrpcServerForUserGrpcClientServiceIT();
	}*/
}
