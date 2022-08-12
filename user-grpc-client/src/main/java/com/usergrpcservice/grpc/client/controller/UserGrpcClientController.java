package com.usergrpcservice.grpc.client.controller;

import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.usergrpcservice.grpc.client.dto.UserRequestDto;
import com.usergrpcservice.grpc.client.dto.UserResponseDto;
import com.usergrpcservice.grpc.client.dto.UserSearchResponseDto;
import com.usergrpcservice.grpc.client.service.UserGrpcClientService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/v1/api/users")
@AllArgsConstructor
public class UserGrpcClientController {

	private final UserGrpcClientService userGrpcClientService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponseDto addUser(@Valid @RequestBody UserRequestDto userRequestDto) {
		return userGrpcClientService.addUser(userRequestDto);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public UserResponseDto updateUser(@PathVariable String id, @NotEmpty @RequestBody UserRequestDto userRequestDto) {
		return userGrpcClientService.updateUser(id, userRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteUser(@PathVariable String id) {
		userGrpcClientService.deleteUser(id);
	}

	@GetMapping("/search")
	@ResponseBody
	public UserSearchResponseDto findUsers(@RequestParam(required = false) Map<String, String> queryParams) {
		int pageNo = Integer.parseInt(queryParams.getOrDefault("page", "0"));
		int pageSize = Integer.parseInt(queryParams.getOrDefault("size", "0"));
		String query = queryParams.keySet().stream().map(key -> key + "=" + queryParams.get(key))
				.collect(Collectors.joining("&"));
		return userGrpcClientService.findUsers(pageNo, pageSize, query);
	}
}
