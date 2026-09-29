package com.thantzin.thantflash.review;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_log")
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    protected ReviewLog() {
    }

    public ReviewLog(Long cardId, Long userId, Grade grade, Instant reviewedAt) {
        this.cardId = cardId;
        this.userId = userId;
        this.grade = grade;
        this.reviewedAt = reviewedAt;
    }

    public Grade getGrade() {
        return grade;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
