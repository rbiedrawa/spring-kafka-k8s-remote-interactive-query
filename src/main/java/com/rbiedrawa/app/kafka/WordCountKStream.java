package com.rbiedrawa.app.kafka;

import java.util.Arrays;
import java.util.function.Function;

import com.rbiedrawa.app.kafka.config.StateStores;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class WordCountKStream {

    private static final String WORDS_SPLIT_REGEX = "\\W+";

    @Bean
    public Function<KStream<String, String>, KStream<String, Long>> wordCountFunction() {
        return input -> input
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split(WORDS_SPLIT_REGEX)))
                .map((key, value) -> new KeyValue<>(value, value))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .count(Materialized.as(StateStores.WORD_COUNT_STATE_STORE))
                .toStream()
                .peek((key, value) -> log.info("---> Key: {} count: {}", key, value));
    }
}