package com.usergrpcservice.grpc.server.model.event;

import com.usergrpcservice.grpc.server.model.UserEntity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatedUserEntityEvent {
	private String eventName;
	private String message;
	private UserEntity userEntity;
}