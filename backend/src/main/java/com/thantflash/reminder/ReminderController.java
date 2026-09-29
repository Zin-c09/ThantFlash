package com.thantflash.reminder;

import com.thantflash.reminder.ReminderDtos.ReminderRequest;
import com.thantflash.reminder.ReminderDtos.ReminderResponse;
import com.thantflash.user.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<ReminderResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return reminderService.list(CurrentUser.id(jwt)).stream().map(ReminderResponse::of).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReminderRequest req) {
        return ReminderResponse.of(reminderService.create(CurrentUser.id(jwt), req));
    }

    @PutMapping("/{id}")
    public ReminderResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                   @Valid @RequestBody ReminderRequest req) {
        return ReminderResponse.of(reminderService.update(CurrentUser.id(jwt), id, req));
    }

    @PostMapping("/{id}/toggle")
    public ReminderResponse toggle(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return ReminderResponse.of(reminderService.toggleDone(CurrentUser.id(jwt), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        reminderService.delete(CurrentUser.id(jwt), id);
    }
}
