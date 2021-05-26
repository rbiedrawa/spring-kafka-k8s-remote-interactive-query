package com.rbiedrawa.app.core.events;

public class EventPublisherException extends RuntimeException {

	private static final String FAILED_TO_SEND_DATA_SYNC_MSG = "Failed to send event synchronously. Topic: %s key: %s value: %s";

	public EventPublisherException(String message, Throwable cause) {
		super(message, cause);
	}

	public static EventPublisherException sendSyncFailure(String topic, String key, Object event,
			Throwable ex) {
		return new EventPublisherException(String.format(FAILED_TO_SEND_DATA_SYNC_MSG, topic, key, event), ex);
	}

}