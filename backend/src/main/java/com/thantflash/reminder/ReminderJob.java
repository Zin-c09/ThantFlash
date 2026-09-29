package com.thantflash.reminder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls for due reminders. With several app instances, add ShedLock (or
 * {@code SELECT ... FOR UPDATE SKIP LOCKED}) so each reminder fires once.
 */
@Component
@ConditionalOnProperty(name = "thantflash.reminders.enabled", havingValue = "true", matchIfMissing = true)
public class ReminderJob {

    private final ReminderService reminderService;

    public ReminderJob(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(fixedDelayString = "${thantflash.reminders.poll-interval:30s}")
    public void run() {
        while (reminderService.fireDue() == ReminderService.BATCH) {
            // keep draining while full batches come back
        }
    }
}
