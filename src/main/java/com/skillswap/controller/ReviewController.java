package com.skillswap.controller;

import com.skillswap.dto.ReviewRequest;
import com.skillswap.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "${app.cors.origins}")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> add(Authentication auth, @RequestBody ReviewRequest req) {
        try {
            return ResponseEntity.ok(service.add((Long) auth.getPrincipal(), req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> forUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.forUser(userId));
    }
}