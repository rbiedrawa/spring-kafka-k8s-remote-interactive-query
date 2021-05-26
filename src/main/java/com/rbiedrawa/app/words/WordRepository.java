package com.rbiedrawa.app.words;

import java.util.List;

public interface WordRepository {
    List<WordCount> findAll();
    WordCount findOne(String key);
}
