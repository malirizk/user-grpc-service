package com.usergrpcservice.grpc.server.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.usergrpcservice.grpc.server.model.UserEntity;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {

	Optional<UserEntity> findByNickname(String nickname);
}
