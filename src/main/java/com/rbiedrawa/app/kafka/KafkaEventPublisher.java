package com.rbiedrawa.app.kafka;

import com.rbiedrawa.app.core.events.EventPublisher;
import com.rbiedrawa.app.core.events.EventPublisherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;

	@Override
	public void publishSync(String topic, String key, String payload) {
		try {
			var record = new ProducerRecord<>(topic, key, payload);
			kafkaTemplate.send(record).get();
			log.info("Successfully send record {}", record);
		} catch (Exception ex) {
			throw EventPublisherException.sendSyncFailure(topic, key, payload, ex);
		}
	}
}
