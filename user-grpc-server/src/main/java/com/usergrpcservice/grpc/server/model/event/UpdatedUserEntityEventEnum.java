package com.usergrpcservice.grpc.server.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UpdatedUserEntityEventEnum {

	CREATED("New user has been created with ID: %s"), UPDATED("User with ID: %s has been updated"), DELETED(
			"User with ID: %s has been deleted");

	private String messageTemplate;
}
