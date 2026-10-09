package com.thantzin.thantflash.card;

import java.time.Instant;

import com.thantzin.thantflash.deck.Deck;
import com.thantzin.thantflash.review.Schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id")
    private Deck deck;

    @Column(nullable = false, length = 2000)
    private String front;

    @Column(nullable = false, length = 2000)
    private String back;

    @Column(name = "interval_days", nullable = false)
    private double intervalDays;

    @Column(nullable = false)
    private double ease;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(nullable = false)
    private int reps;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Card() {
    }

    /** A brand-new card is due immediately. */
    public Card(Deck deck, String front, String back, Instant now) {
        this.deck = deck;
        this.front = front;
        this.back = back;
        this.intervalDays = 0;
        this.ease = Schedule.INITIAL_EASE;
        this.dueAt = now;
        this.reps = 0;
        this.createdAt = now;
    }

    public void edit(Deck deck, String front, String back) {
        this.deck = deck;
        this.front = front;
        this.back = back;
    }

    public Schedule schedule() {
        return new Schedule(intervalDays, ease, dueAt);
    }

    public void applyReview(Schedule next) {
        this.intervalDays = next.intervalDays();
        this.ease = next.ease();
        this.dueAt = next.dueAt();
        this.reps++;
    }

    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public double getIntervalDays() {
        return intervalDays;
    }

    public double getEase() {
        return ease;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public int getReps() {
        return reps;
    }
}
