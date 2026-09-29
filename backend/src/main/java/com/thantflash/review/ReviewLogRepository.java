package com.thantflash.review;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    @Query("select r from ReviewLog r where r.user.id = :userId and r.reviewedAt >= :since order by r.reviewedAt")
    List<ReviewLog> findSince(Long userId, Instant since);
}
