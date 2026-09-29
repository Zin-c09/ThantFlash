package com.thantflash.reminder;

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
import java.time.Instant;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(name = "fire_at", nullable = false)
    private Instant fireAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Repeat repeat;

    @Column(nullable = false)
    private boolean done;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Reminder() {
    }

    public Reminder(User owner, String title, Instant fireAt, Repeat repeat) {
        this.owner = owner;
        this.title = title;
        this.fireAt = fireAt;
        this.repeat = repeat;
    }

    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getFireAt() { return fireAt; }
    public void setFireAt(Instant fireAt) { this.fireAt = fireAt; }
    public Repeat getRepeat() { return repeat; }
    public void setRepeat(Repeat repeat) { this.repeat = repeat; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
