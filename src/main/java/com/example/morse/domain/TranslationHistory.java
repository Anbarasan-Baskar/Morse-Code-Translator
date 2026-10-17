package com.example.morse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "translation_history")
public class TranslationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TranslationDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private TranslationSource source;

    @Lob
    @Column(nullable = false)
    private String inputText;

    @Lob
    @Column(nullable = false)
    private String outputText;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TranslationHistory() { }

    public TranslationHistory(AppUser user, TranslationDirection direction,
                              TranslationSource source, String inputText, String outputText) {
        this.user = user;
        this.direction = direction;
        this.source = source;
        this.inputText = inputText;
        this.outputText = outputText;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public TranslationDirection getDirection() { return direction; }
    public TranslationSource getSource() { return source; }
    public String getInputText() { return inputText; }
    public String getOutputText() { return outputText; }
    public Instant getCreatedAt() { return createdAt; }
}
