package com.rbiedrawa.app.kafka;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.rbiedrawa.app.kafka.config.StateStores;
import com.rbiedrawa.app.words.WordCount;
import com.rbiedrawa.app.words.WordRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Slf4j
@Service
@AllArgsConstructor
public class KafkaWordRepository implements WordRepository {

	private static final String FIND_ONE_ENDPOINT_REGEX = "http://%s:%d/words/%s";

	private final InteractiveQueryService interactiveQueryService;

	private final RestTemplate restTemplate;

	@Override
	public List<WordCount> findAll() {
		List<WordCount> wordCounts = new ArrayList<>();
		ReadOnlyKeyValueStore<String, Long> queryableStore = interactiveQueryService.getQueryableStore(StateStores.WORD_COUNT_STATE_STORE, QueryableStoreTypes.keyValueStore());
		queryableStore.all().forEachRemaining(record -> wordCounts.add(new WordCount(record.key, record.value)));
		return wordCounts;
	}

	@Override
	public WordCount findOne(String key) {
		var hostInfo = interactiveQueryService.getHostInfo(StateStores.WORD_COUNT_STATE_STORE,
														   key, new StringSerializer());
		var isInCurrentHost = interactiveQueryService.getCurrentHostInfo().equals(hostInfo);
		return isInCurrentHost ? findOneFromLocalStateStore(key) : findOneFromRemoteStateStore(key, hostInfo);
	}

	private WordCount findOneFromRemoteStateStore(String key, HostInfo hostInfo) {
		log.info("Handling key {} from different host {}", key, hostInfo);
		return restTemplate.getForObject(String.format(FIND_ONE_ENDPOINT_REGEX, hostInfo.host(),
													   hostInfo.port(), key), WordCount.class);
	}

	private WordCount findOneFromLocalStateStore(String key) {
		log.info("Handling key {} from current host", key);
		ReadOnlyKeyValueStore<String, Long> queryableStore = interactiveQueryService.getQueryableStore(StateStores.WORD_COUNT_STATE_STORE, QueryableStoreTypes.keyValueStore());
		var count = queryableStore.get(key);
		return new WordCount(key, count);
	}

}
