package com.skillswap.repository;

import com.skillswap.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsBySessionIdAndReviewerId(Long sessionId, Long reviewerId);
    List<Review> findByRevieweeId(Long revieweeId);
}