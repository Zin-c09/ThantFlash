package com.thantflash.reminder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByOwnerIdOrderByDoneAscFireAtAsc(Long ownerId);

    Optional<Reminder> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("select r from Reminder r join fetch r.owner where r.done = false and r.fireAt <= :now order by r.fireAt")
    List<Reminder> findFiring(Instant now, Pageable batch);
}
