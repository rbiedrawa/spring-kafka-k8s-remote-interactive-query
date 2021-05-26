package com.rbiedrawa.app.web.words;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rbiedrawa.app.core.events.EventPublisher;
import com.rbiedrawa.app.kafka.config.KafkaTopics;
import com.rbiedrawa.app.words.WordCount;
import com.rbiedrawa.app.words.WordRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("words")
public class WordsController {

	private final EventPublisher eventPublisher;
	private final WordRepository wordRepository;

	@Operation(summary = "Get all word counts from local state store")
	@GetMapping
	public ResponseEntity<List<WordCount>> findAll() {
		var wordCountsList = wordRepository.findAll();
		return ResponseEntity.ok(wordCountsList);
	}

	@Operation(summary = "Publish words message to topic")
	@PostMapping
	public ResponseEntity<WordsDTO> create(@RequestBody WordsDTO words) {
		log.info("Received words: {}", words);
		eventPublisher.publishSync(KafkaTopics.INPUT, null, words.getWords());
		return ResponseEntity.ok(words);
	}

	@Operation(summary = "Get word counts from local or remote state store")
	@GetMapping("{key}")
	public ResponseEntity<WordCount> findOne(@PathVariable String key) {
		var wordCountDTO = wordRepository.findOne(key.toLowerCase());
		return ResponseEntity.ok(wordCountDTO);
	}
}
