package com.usergrpcservice.grpc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserGrpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserGrpcServerApplication.class, args);
    }
}