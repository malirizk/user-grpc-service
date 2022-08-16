package com.usergrpcservice.grpc.server.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;

import com.google.protobuf.Empty;
import com.usergrpcservice.grpc.*;
import com.usergrpcservice.grpc.server.exception.BusinessException;
import com.usergrpcservice.grpc.server.exception.ExceptionMap;
import com.usergrpcservice.grpc.server.mapper.UserMapper;
import com.usergrpcservice.grpc.server.model.SearchKeyEnum;
import com.usergrpcservice.grpc.server.model.UserEntity;
import com.usergrpcservice.grpc.server.model.event.UpdatedUserEntityEvent;
import com.usergrpcservice.grpc.server.model.event.UpdatedUserEntityEventEnum;
import com.usergrpcservice.grpc.server.repository.UserEntityRepository;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcServerService extends UserServiceGrpc.UserServiceImplBase {

	private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
	private final UserMapper userMapper;
	private final UserEntityRepository userEntityRepository;
	private final Validator validator;
	private final UserProducerEventService userProducerEventService;
	@Value("${user.service.search.page.size:10}")
	private Integer DEFAULT_SEARCH_PAGE_SIZE;
	@Value("${user.service.search.sort.by:createdAt}")
	private String DEFAULT_SEARCH_SORT_BY_COLUMNS;

	@Override
	public void addUser(AddUserRequest request, StreamObserver<UserResponse> responseObserver) {
		UserEntity user = userMapper.toUserEntity(request);

		Set<ConstraintViolation<UserEntity>> violationSet = validator.validate(user);
		if (!violationSet.isEmpty())
			throw new ConstraintViolationException(violationSet);
		userEntityRepository.findByNickname(user.getNickname()).ifPresent(s -> {
			throw new BusinessException(ExceptionMap.NICKNAME_ALREADY_EXIST);
		});

		userEntityRepository.save(user);

		String message = String.format(UpdatedUserEntityEventEnum.CREATED.getMessageTemplate(), user.getId());
		UpdatedUserEntityEvent userEntityEvent = UpdatedUserEntityEvent.builder()
				.eventName(UpdatedUserEntityEventEnum.CREATED.name()).message(message).userEntity(user).build();
		userProducerEventService.send(userEntityEvent);

		UserResponse addUserResponse = userMapper.toUserResponse(user);
		responseObserver.onNext(addUserResponse);
		responseObserver.onCompleted();
	}

	public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
		Optional<UserEntity> userEntityOptional;
		if (StringUtils.isBlank(request.getId()) || !request.getId().matches(UUID_REGEX)
				|| (userEntityOptional = userEntityRepository.findById(UUID.fromString(request.getId()))).isEmpty()) {
			throw new BusinessException(ExceptionMap.USER_NOT_FOUND);
		}

		UserEntity user = userEntityOptional.get();
		Optional<UserEntity> requestNicknameUser;

		if (StringUtils.isNoneBlank(request.getNickname()) && !user.getNickname().equals(request.getNickname())
				&& (requestNicknameUser = userEntityRepository.findByNickname(request.getNickname())).isPresent()
				&& !requestNicknameUser.get().getId().equals(user.getId())) {
			throw new BusinessException(ExceptionMap.NICKNAME_ALREADY_EXIST);
		}

		userMapper.updateUserEntityFromUpdateUserRequest(request, user);
		log.info("Before update user ID {} in DB", user.getId());
		userEntityRepository.save(user);

		String message = String.format(UpdatedUserEntityEventEnum.UPDATED.getMessageTemplate(), user.getId());
		UpdatedUserEntityEvent userEntityEvent = UpdatedUserEntityEvent.builder()
				.eventName(UpdatedUserEntityEventEnum.UPDATED.name()).message(message).userEntity(user).build();
		userProducerEventService.send(userEntityEvent);

		UserResponse addUserResponse = userMapper.toUserResponse(user);
		responseObserver.onNext(addUserResponse);
		responseObserver.onCompleted();
	}

	public void deleteUser(DeleteRequest request, StreamObserver<Empty> responseObserver) {
		if (StringUtils.isBlank(request.getId()) || !request.getId().matches(UUID_REGEX)) {
			throw new IllegalArgumentException("User ID is not valid!");
		}

		Optional<UserEntity> user = userEntityRepository.findById(UUID.fromString(request.getId()));
		if (user.isPresent()) {
			String message = String.format(UpdatedUserEntityEventEnum.UPDATED.getMessageTemplate(), user.get().getId());
			UpdatedUserEntityEvent userEntityEvent = UpdatedUserEntityEvent.builder()
					.eventName(UpdatedUserEntityEventEnum.UPDATED.name()).message(message).userEntity(user.get())
					.build();
			userProducerEventService.send(userEntityEvent);

			userEntityRepository.delete(user.get());
		} else {
			throw new BusinessException(ExceptionMap.USER_NOT_FOUND);
		}

		responseObserver.onNext(Empty.newBuilder().build());
		responseObserver.onCompleted();
	}

	public void listUsers(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
		UserEntity userSearchEntity = new UserEntity();
		fillSearchCriteria(request.getQuery(), userSearchEntity);

		Pageable pageable = PageRequest.of(
				request.getPageNumber(), Optional.of(request.getResultPerPage())
						.map(s -> s == 0 ? DEFAULT_SEARCH_PAGE_SIZE : s).orElse(DEFAULT_SEARCH_PAGE_SIZE),
				Sort.by(DEFAULT_SEARCH_SORT_BY_COLUMNS));
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll().withIgnoreCase()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
		Example<UserEntity> example = Example.of(userSearchEntity, exampleMatcher);

		Page<UserEntity> userEntityPage = userEntityRepository.findAll(example, pageable);

		List<UserResponse> userResponses = userEntityPage.get().map(userMapper::toUserResponse)
				.collect(Collectors.toList());
		SearchResponse response = SearchResponse.newBuilder().setTotalPages(userEntityPage.getTotalPages())
				.setPageNumber(userEntityPage.getNumber()).setTotalResult((int) userEntityPage.getTotalElements())
				.addAllContent(userResponses).build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	private void fillSearchCriteria(String search, UserEntity userSearchEntity) {
		String[] searchItemList = search.split("&");
		for (String searchItem : searchItemList) {
			String[] searchKeyValue = searchItem.split("=");
			String searchKey = searchKeyValue[0];

			if (SearchKeyEnum.FIRST_NAME.getKey().equalsIgnoreCase(searchKey)) {
				userSearchEntity.setFirstName(searchKeyValue[1]);
			} else if (SearchKeyEnum.LAST_NAME.getKey().equalsIgnoreCase(searchKey)) {
				userSearchEntity.setLastName(searchKeyValue[1]);
			} else if (SearchKeyEnum.NICKNAME.getKey().equalsIgnoreCase(searchKey)) {
				userSearchEntity.setNickname(searchKeyValue[1]);
			} else if (SearchKeyEnum.EMAIL.getKey().equalsIgnoreCase(searchKey)) {
				userSearchEntity.setEmail(searchKeyValue[1]);
			} else if (SearchKeyEnum.COUNTRY.getKey().equalsIgnoreCase(searchKey)) {
				userSearchEntity.setCountry(searchKeyValue[1]);
			}
		}
	}
}