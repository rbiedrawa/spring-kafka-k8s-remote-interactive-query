package com.rbiedrawa.app.core.events;


public interface EventPublisher {

	void publishSync(String topic, String key, String payload);

}
