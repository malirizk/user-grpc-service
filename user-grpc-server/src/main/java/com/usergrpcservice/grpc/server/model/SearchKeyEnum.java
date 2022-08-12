package com.usergrpcservice.grpc.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchKeyEnum {

	FIRST_NAME("first_name", "firstName"), LAST_NAME("last_name", "lastName"), NICKNAME("nickname",
			"nickname"), EMAIL("email", "email"), COUNTRY("country", "country");

	private String key, value;
}
