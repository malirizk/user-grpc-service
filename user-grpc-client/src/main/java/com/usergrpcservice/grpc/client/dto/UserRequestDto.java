package com.usergrpcservice.grpc.client.dto;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

	@NotBlank
	@JsonProperty("first_name")
	private String firstName;
	@NotBlank
	@JsonProperty("last_name")
	private String lastName;
	@NotBlank
	private String nickname;
	@NotBlank
	private String password;
	@NotBlank
	private String email;
	@NotBlank
	private String country;
}
