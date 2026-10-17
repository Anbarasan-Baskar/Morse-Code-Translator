package com.example.morse.api;

import com.example.morse.domain.TranslationDirection;
import com.example.morse.domain.TranslationSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TranslationRequest(
        @NotNull TranslationDirection direction,
        @NotNull TranslationSource source,
        @NotBlank @Size(max = 50_000) String inputText) { }
