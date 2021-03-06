package com.co.kafkapoc.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.co.kafkapoc.constants.KafkaPocConstants;
import com.co.kafkapoc.dto.MessageDto;
import com.co.kafkapoc.dto.TestTypesDto;

import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializerConfig;

@Configuration
@EnableKafka
public class KafkaConfig
{
	@Value("${kafka.bootstrap.url}")
	private String bootstrapServersUrl;

	@Value("${kafka.groupId}")
	private String groupId;

	@Value("${kafka.schemaRegistryUrl}")
	private String schemaRegistryUrl;

	@Value("${kafka.autoRegisterSchemas}")
	private boolean autoRegisterSchemas;

	@Value("${webhook.exponential-backoff.initial:1000}")
	long exponentialBackoffInitial;

	@Value("${webhook.exponential-backoff.max:10000}")
	long exponentialBackoffMax;

	@Value("${webhook.exponential-backoff.multiplier:2.0}")
	double exponentialBackoffMultiplier;

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory()
	{
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(getProperties(true)));
		return factory;
	}

	@Bean
	public KafkaTemplate<String, MessageDto> messageDtoKafkaTemplate()
	{
		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(getProperties(false)));
	}

	@Bean
	public KafkaTemplate<String, TestTypesDto> testTypesDtoKafkaTemplate()
	{
		DefaultKafkaProducerFactory<String, TestTypesDto> defaultKafkaProducerFactory = new DefaultKafkaProducerFactory<>(getProperties(false));
		return new KafkaTemplate<>(defaultKafkaProducerFactory);
	}

	@Bean
	public RetryTemplate retryTemplate()
	{
		RetryTemplate retryTemplate = new RetryTemplate();
		AlwaysRetryPolicy alwaysRetryPolicy = new AlwaysRetryPolicy();

		ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
		exponentialBackOffPolicy.setInitialInterval(exponentialBackoffInitial);
		exponentialBackOffPolicy.setMultiplier(exponentialBackoffMultiplier);
		exponentialBackOffPolicy.setMaxInterval(exponentialBackoffMax);

		retryTemplate.setRetryPolicy(alwaysRetryPolicy);
		retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

		return retryTemplate;
	}

	private Map<String, Object> getProperties(Boolean isConsumer)
	{
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersUrl);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(KafkaPocConstants.SCHEMA_REGISTRY_URL, schemaRegistryUrl);

		if (isConsumer)
		{
			props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
			props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
			props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
			props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaJsonSchemaDeserializer.class);
			props.put(KafkaJsonDeserializerConfig.JSON_VALUE_TYPE, MessageDto.class);
			props.put(KafkaPocConstants.JSON_FAIL_INVALID_SCHEMA, true);
		}
		else
		{
			props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
			props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
			props.put(KafkaPocConstants.AUTO_REGISTER_SCHEMAS, autoRegisterSchemas);
			props.put(KafkaJsonSchemaSerializerConfig.WRITE_DATES_AS_ISO8601, true);
		}
		return props;
	}
}
