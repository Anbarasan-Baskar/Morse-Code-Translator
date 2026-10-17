package com.example.morse.service;

import com.example.morse.domain.AppUser;
import com.example.morse.domain.TranslationDirection;
import com.example.morse.domain.TranslationHistory;
import com.example.morse.domain.TranslationSource;
import com.example.morse.repository.AppUserRepository;
import com.example.morse.repository.TranslationHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TranslationService {
    public static final int MAX_INPUT_CHARACTERS = 50_000;
    private final TranslationHistoryRepository history;
    private final AppUserRepository users;

    public TranslationService(TranslationHistoryRepository history, AppUserRepository users) {
        this.history = history;
        this.users = users;
    }

    @Transactional
    public TranslationHistory translate(Long userId, TranslationDirection direction,
                                         TranslationSource source, String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Enter some text to translate.");
        }
        if (inputText.length() > MAX_INPUT_CHARACTERS) {
            throw new IllegalArgumentException("Input must be 50,000 characters or less.");
        }
        String output = switch (direction) {
            case TEXT_TO_MORSE -> MorseCodec.encode(inputText);
            case MORSE_TO_TEXT -> MorseCodec.decode(inputText);
        };
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Signed-in account no longer exists."));
        return history.save(new TranslationHistory(user, direction, source, inputText, output));
    }

    @Transactional(readOnly = true)
    public List<TranslationHistory> recentFor(Long userId) {
        return history.findTop50ByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public boolean deleteOne(Long userId, Long historyId) {
        return history.findByIdAndUser_Id(historyId, userId)
                .map(entry -> {
                    history.delete(entry);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public void clear(Long userId) {
        history.deleteByUser_Id(userId);
    }
}
