package com.example.morse.api;

import com.example.morse.domain.TranslationHistory;
import com.example.morse.security.AppUserDetails;
import com.example.morse.service.TranslationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {
    private final TranslationService translations;

    public TranslationController(TranslationService translations) {
        this.translations = translations;
    }

    @PostMapping
    public TranslationResponse translate(@AuthenticationPrincipal AppUserDetails user,
                                         @Valid @RequestBody TranslationRequest request) {
        TranslationHistory history = translations.translate(
                user.getId(), request.direction(), request.source(), request.inputText());
        return TranslationResponse.from(history);
    }
}
