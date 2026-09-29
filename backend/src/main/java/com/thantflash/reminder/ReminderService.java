package com.thantflash.reminder;

import com.thantflash.common.NotFoundException;
import com.thantflash.reminder.ReminderDtos.ReminderRequest;
import com.thantflash.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderService {

    static final int BATCH = 100;

    private final ReminderRepository reminders;
    private final UserRepository users;
    private final ReminderNotifier notifier;
    private final Clock clock;

    public ReminderService(ReminderRepository reminders, UserRepository users,
                           ReminderNotifier notifier, Clock clock) {
        this.reminders = reminders;
        this.users = users;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Reminder> list(long userId) {
        return reminders.findByOwnerIdOrderByDoneAscFireAtAsc(userId);
    }

    @Transactional
    public Reminder create(long userId, ReminderRequest req) {
        return reminders.save(new Reminder(users.getReferenceById(userId), req.title().trim(), req.fireAt(),
                req.repeat() == null ? Repeat.NONE : req.repeat()));
    }

    @Transactional
    public Reminder update(long userId, long id, ReminderRequest req) {
        Reminder r = get(userId, id);
        r.setTitle(req.title().trim());
        r.setFireAt(req.fireAt());
        r.setRepeat(req.repeat() == null ? Repeat.NONE : req.repeat());
        return r;
    }

    @Transactional
    public Reminder toggleDone(long userId, long id) {
        Reminder r = get(userId, id);
        r.setDone(!r.isDone());
        return r;
    }

    @Transactional
    public void delete(long userId, long id) {
        reminders.delete(get(userId, id));
    }

    private Reminder get(long userId, long id) {
        return reminders.findByIdAndOwnerId(id, userId).orElseThrow(() -> new NotFoundException("Reminder", id));
    }

    /**
     * Fires every due reminder once: one-off reminders are marked done,
     * repeating ones roll forward to their next future occurrence.
     *
     * @return number of reminders fired
     */
    @Transactional
    public int fireDue() {
        Instant now = clock.instant();
        List<Reminder> due = reminders.findFiring(now, PageRequest.of(0, BATCH));
        for (Reminder r : due) {
            notifier.notify(r);
            if (r.getRepeat() == Repeat.NONE) {
                r.setDone(true);
            } else {
                r.setFireAt(r.getRepeat().nextAfter(r.getFireAt(), now, ZoneId.of(r.getOwner().getTimeZone())));
            }
        }
        return due.size();
    }
}
