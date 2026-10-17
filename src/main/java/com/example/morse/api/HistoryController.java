package com.example.morse.api;

import com.example.morse.security.AppUserDetails;
import com.example.morse.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final TranslationService translations;

    public HistoryController(TranslationService translations) {
        this.translations = translations;
    }

    @GetMapping
    public List<TranslationResponse> recent(@AuthenticationPrincipal AppUserDetails user) {
        return translations.recentFor(user.getId()).stream().map(TranslationResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOne(@AuthenticationPrincipal AppUserDetails user,
                                          @PathVariable Long id) {
        return translations.deleteOne(user.getId(), id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal AppUserDetails user) {
        translations.clear(user.getId());
        return ResponseEntity.noContent().build();
    }
}
