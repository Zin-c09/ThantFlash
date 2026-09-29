package com.thantflash.reminder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ReminderDtos {

    private ReminderDtos() {
    }

    public record ReminderRequest(
            @NotBlank @Size(max = 200) String title,
            @NotNull Instant fireAt,
            Repeat repeat) {
    }

    public record ReminderResponse(long id, String title, Instant fireAt, Repeat repeat, boolean done) {

        static ReminderResponse of(Reminder r) {
            return new ReminderResponse(r.getId(), r.getTitle(), r.getFireAt(), r.getRepeat(), r.isDone());
        }
    }
}
