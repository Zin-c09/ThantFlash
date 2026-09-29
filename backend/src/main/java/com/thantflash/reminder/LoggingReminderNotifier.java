package com.thantflash.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingReminderNotifier implements ReminderNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingReminderNotifier.class);

    @Override
    public void notify(Reminder reminder) {
        log.info("Reminder {} for user {}: {}", reminder.getId(), reminder.getOwner().getId(), reminder.getTitle());
    }
}
