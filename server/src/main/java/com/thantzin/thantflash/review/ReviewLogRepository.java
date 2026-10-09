package com.thantzin.thantflash.review;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    List<ReviewLog> findByUserIdAndReviewedAtGreaterThanEqual(Long userId, Instant from);
}
