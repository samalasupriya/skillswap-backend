package com.skillswap.controller;

import com.skillswap.dto.ProposeSessionRequest;
import com.skillswap.service.SwapSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "http://localhost:5173")
public class SwapSessionController {

    private final SwapSessionService service;

    public SwapSessionController(SwapSessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> propose(Authentication auth, @RequestBody ProposeSessionRequest req) {
        try {
            return ResponseEntity.ok(service.propose((Long) auth.getPrincipal(), req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> mine(Authentication auth) {
        return ResponseEntity.ok(service.mine((Long) auth.getPrincipal()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(Authentication auth, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.confirm((Long) auth.getPrincipal(), id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(Authentication auth, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.complete((Long) auth.getPrincipal(), id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(Authentication auth, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.cancel((Long) auth.getPrincipal(), id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}