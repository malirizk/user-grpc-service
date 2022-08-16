package com.usergrpcservice.grpc.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.usergrpcservice.grpc.server.model.UserEntity;
import com.usergrpcservice.grpc.server.model.event.UpdatedUserEntityEvent;
import com.usergrpcservice.grpc.server.model.event.UpdatedUserEntityEventEnum;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = {"listeners=PLAINTEXT://localhost:9095",
		"port=9095"})
class UserProducerEventServiceIT {

	Consumer<String, UpdatedUserEntityEvent> consumer;
	private UserProducerEventService userProducerEventService;
	@Value("${user.service.update.event.topic}")
	private String USER_ENTITY_UPDATES_TOPIC_NAME;
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@BeforeEach
	void setUp() {
		Map<String, Object> configProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
		configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		JsonDeserializer<UpdatedUserEntityEvent> jsonDeserializer = new JsonDeserializer<>(
				UpdatedUserEntityEvent.class);
		jsonDeserializer.setRemoveTypeHeaders(false);
		jsonDeserializer.addTrustedPackages("*");
		jsonDeserializer.setUseTypeMapperForKey(true);

		consumer = new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), jsonDeserializer)
				.createConsumer();
		consumer.subscribe(Collections.singleton(USER_ENTITY_UPDATES_TOPIC_NAME));

		userProducerEventService = new UserProducerEventService(kafkaTemplate());
		userProducerEventService.setUSER_ENTITY_UPDATES_TOPIC_NAME(USER_ENTITY_UPDATES_TOPIC_NAME);
	}

	@Test
	void Should_Success_When_Send_Event() {
		UserEntity userEntity = new UserEntity();
		UpdatedUserEntityEvent userEntityEvent = UpdatedUserEntityEvent.builder()
				.eventName(UpdatedUserEntityEventEnum.CREATED.name())
				.message(UpdatedUserEntityEventEnum.CREATED.getMessageTemplate()).userEntity(userEntity).build();
		userProducerEventService.send(userEntityEvent);

		ConsumerRecord<String, UpdatedUserEntityEvent> singleRecord = KafkaTestUtils.getSingleRecord(consumer,
				USER_ENTITY_UPDATES_TOPIC_NAME);
		assertThat(singleRecord).isNotNull();
		assertEquals(userEntityEvent.getEventName(), singleRecord.value().getEventName());
		assertEquals(userEntityEvent.getMessage(), singleRecord.value().getMessage());
		assertEquals(userEntityEvent.getUserEntity(), singleRecord.value().getUserEntity());

		consumer.close();
	}

	private ProducerFactory<String, UpdatedUserEntityEvent> producerFactory() {
		Map<String, Object> configProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return new DefaultKafkaProducerFactory<String, UpdatedUserEntityEvent>(configProps);
	}

	private KafkaTemplate<String, UpdatedUserEntityEvent> kafkaTemplate() {
		KafkaTemplate<String, UpdatedUserEntityEvent> kafkaTemplate = new KafkaTemplate<>(producerFactory());
		kafkaTemplate.setDefaultTopic(USER_ENTITY_UPDATES_TOPIC_NAME);
		return kafkaTemplate;
	}

	@Test
	void testEmbeddedKafka() {
		assertTrue(true);
		Consumer<Integer, String> consumer = configureConsumer();
		Producer<Integer, String> producer = configureProducer();

		producer.send(new ProducerRecord<>(USER_ENTITY_UPDATES_TOPIC_NAME, 123, "my-test-value"));

		ConsumerRecord<Integer, String> singleRecord = KafkaTestUtils.getSingleRecord(consumer,
				USER_ENTITY_UPDATES_TOPIC_NAME);
		assertThat(singleRecord).isNotNull();
		assertThat(singleRecord.key()).isEqualTo(123);
		assertThat(singleRecord.value()).isEqualTo("my-test-value");

		consumer.close();
		producer.close();
	}

	private Consumer<Integer, String> configureConsumer() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		Consumer<Integer, String> consumer = new DefaultKafkaConsumerFactory<Integer, String>(consumerProps)
				.createConsumer();
		consumer.subscribe(Collections.singleton(USER_ENTITY_UPDATES_TOPIC_NAME));
		return consumer;
	}

	private Producer<Integer, String> configureProducer() {
		Map<String, Object> producerProps = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
		return new DefaultKafkaProducerFactory<Integer, String>(producerProps).createProducer();
	}
}
