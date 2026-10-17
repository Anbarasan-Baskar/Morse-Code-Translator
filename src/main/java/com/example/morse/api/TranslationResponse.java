package com.example.morse.api;

import com.example.morse.domain.TranslationDirection;
import com.example.morse.domain.TranslationHistory;
import com.example.morse.domain.TranslationSource;

import java.time.Instant;

public record TranslationResponse(
        Long id,
        TranslationDirection direction,
        TranslationSource source,
        String inputText,
        String outputText,
        Instant createdAt) {
    public static TranslationResponse from(TranslationHistory history) {
        return new TranslationResponse(history.getId(), history.getDirection(), history.getSource(),
                history.getInputText(), history.getOutputText(), history.getCreatedAt());
    }
}
