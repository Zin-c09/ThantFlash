package com.thantflash.review;

import com.thantflash.card.Card;
import com.thantflash.card.Grade;
import com.thantflash.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

/** Append-only history of every answer; the source for study statistics. */
@Entity
@Table(name = "review_logs")
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id")
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.ORDINAL)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private Grade grade;

    @Column(name = "interval_days", nullable = false)
    private double intervalDays;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    protected ReviewLog() {
    }

    public ReviewLog(Card card, User user, Grade grade, double intervalDays, Instant reviewedAt) {
        this.card = card;
        this.user = user;
        this.grade = grade;
        this.intervalDays = intervalDays;
        this.reviewedAt = reviewedAt;
    }

    public Grade getGrade() { return grade; }
    public Instant getReviewedAt() { return reviewedAt; }
}
