package com.rbiedrawa.app.kafka.config.initializer;

import com.rbiedrawa.app.kafka.config.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Component
public class KafkaTopicsBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	public static final int DEFAULT_NUM_PARTITIONS = 6;

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		log.info("Registering new beans");
		var topics = findAllKafkaTopicsNameIn(KafkaTopics.class);
		log.info("Found topics size {} names {}", topics.length, topics);
		Stream.of(topics).forEach(topicName -> beanFactory
				.registerSingleton(StringUtils.capitalize(topicName) + "-NewKafkaTopic", newKafkaTopic(topicName)));
		log.info("Register completed");
	}

	public NewTopic newKafkaTopic(String topicName) {
		return new NewTopic(topicName, DEFAULT_NUM_PARTITIONS, (short) 1);
	}

	public String[] findAllKafkaTopicsNameIn(Class<?> clazz) {
		return Arrays.stream(clazz.getDeclaredFields()).map(field -> {
			try {
				return (String) field.get(clazz);
			}
			catch (IllegalAccessException ex) {
				throw new IllegalStateException("Failed to obtain topic name", ex);
			}
		}).toArray(String[]::new);
	}

}
