package com.skillswap.service;

import com.skillswap.dto.ReviewRequest;
import com.skillswap.model.*;
import com.skillswap.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final SwapSessionRepository sessionRepo;
    private final UserRepository userRepo;

    public ReviewService(ReviewRepository reviewRepo, SwapSessionRepository sessionRepo, UserRepository userRepo) {
        this.reviewRepo = reviewRepo;
        this.sessionRepo = sessionRepo;
        this.userRepo = userRepo;
    }

    public Map<String, Object> add(Long myId, ReviewRequest req) {
        if (req.sessionId() == null || req.rating() < 1 || req.rating() > 5) {
            throw new IllegalArgumentException("sessionId and a rating from 1 to 5 are required");
        }
        SwapSession s = sessionRepo.findById(req.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        ConnectionRequest c = s.getConnection();
        boolean iAmSender = c.getSender().getId().equals(myId);
        boolean iAmReceiver = c.getReceiver().getId().equals(myId);
        if (!iAmSender && !iAmReceiver) {
            throw new IllegalArgumentException("You are not part of this session");
        }
        if (s.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("You can review only after the session is completed");
        }
        if (reviewRepo.existsBySessionIdAndReviewerId(s.getId(), myId)) {
            throw new IllegalArgumentException("You already reviewed this session");
        }
        User me = userRepo.findById(myId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User other = iAmSender ? c.getReceiver() : c.getSender();

        Review r = new Review();
        r.setSession(s);
        r.setReviewer(me);
        r.setReviewee(other);
        r.setRating(req.rating());
        r.setComment(req.comment());
        r = reviewRepo.save(r);
        return Map.of("reviewId", r.getId(), "rating", r.getRating(), "reviewee", other.getName() == null ? "" : other.getName());
    }

    public Map<String, Object> forUser(Long userId) {
        List<Review> list = reviewRepo.findByRevieweeId(userId);
        double avg = list.stream().mapToInt(Review::getRating).average().orElse(0);
        List<Map<String, Object>> items = list.stream().map(r -> Map.<String, Object>of(
                "rating", r.getRating(),
                "comment", r.getComment() == null ? "" : r.getComment(),
                "reviewer", r.getReviewer().getName() == null ? "" : r.getReviewer().getName()
        )).toList();
        return Map.of("userId", userId, "averageRating", avg, "count", list.size(), "reviews", items);
    }
}