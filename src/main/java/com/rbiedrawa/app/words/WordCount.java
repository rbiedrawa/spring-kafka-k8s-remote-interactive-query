package com.rbiedrawa.app.words;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class WordCount {
    private final String word;
    private final Long count;
}
