package com.example.morse.repository;

import com.example.morse.domain.TranslationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TranslationHistoryRepository extends JpaRepository<TranslationHistory, Long> {
    List<TranslationHistory> findTop50ByUser_IdOrderByCreatedAtDesc(Long userId);
    Optional<TranslationHistory> findByIdAndUser_Id(Long id, Long userId);
    long deleteByUser_Id(Long userId);
}
