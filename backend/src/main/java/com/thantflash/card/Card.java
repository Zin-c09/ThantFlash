package com.thantflash.card;

import com.thantflash.deck.Deck;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "cards")
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
    private double ease = 2.5;

    @Column(nullable = false)
    private int reps;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Optimistic locking: two tabs reviewing the same card can't both win. */
    @Version
    private long version;

    protected Card() {
    }

    public Card(Deck deck, String front, String back, Instant now) {
        this.deck = deck;
        this.front = front;
        this.back = back;
        this.dueAt = now;
        this.createdAt = now;
    }

    public Sm2Scheduler.State schedule() {
        return new Sm2Scheduler.State(intervalDays, ease, reps, dueAt);
    }

    public void apply(Sm2Scheduler.State s) {
        this.intervalDays = s.intervalDays();
        this.ease = s.ease();
        this.reps = s.reps();
        this.dueAt = s.due();
    }

    public Long getId() { return id; }
    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }
    public double getIntervalDays() { return intervalDays; }
    public double getEase() { return ease; }
    public int getReps() { return reps; }
    public Instant getDueAt() { return dueAt; }
    public Instant getCreatedAt() { return createdAt; }
}
