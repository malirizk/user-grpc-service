package com.usergrpcservice.grpc.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.usergrpcservice.grpc.server.model.event.UpdatedUserEntityEvent;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProducerEventService {

	private final KafkaTemplate<String, UpdatedUserEntityEvent> kafkaTemplate;

	@Setter
	@Value("${user.service.update.event.topic}")
	private String USER_ENTITY_UPDATES_TOPIC_NAME;

	public void send(UpdatedUserEntityEvent updatedUserEntityEvent) {
		ListenableFuture<SendResult<String, UpdatedUserEntityEvent>> future = kafkaTemplate
				.send(USER_ENTITY_UPDATES_TOPIC_NAME, updatedUserEntityEvent);

		future.addCallback(new ListenableFutureCallback<SendResult<String, UpdatedUserEntityEvent>>() {
			@Override
			public void onSuccess(SendResult<String, UpdatedUserEntityEvent> result) {
				log.info("Sent event=[{}] with offset=[{}]", updatedUserEntityEvent.toString(),
						result.getRecordMetadata().offset());
			}

			@Override
			public void onFailure(Throwable ex) {
				log.error("Unable to send message=[{}] due to : {}", updatedUserEntityEvent.toString(), ex);
			}
		});
	}
}
