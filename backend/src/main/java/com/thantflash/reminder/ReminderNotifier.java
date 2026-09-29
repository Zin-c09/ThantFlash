package com.thantflash.reminder;

/**
 * Delivery channel for due reminders. The default implementation logs; swap in
 * email, Web Push or Telegram by providing a {@code @Primary} bean.
 */
public interface ReminderNotifier {

    void notify(Reminder reminder);
}
