package com.skillswap.controller;

import com.skillswap.service.ConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "http://localhost:5173")
public class ConnectionController {

    private final ConnectionService service;

    public ConnectionController(ConnectionService service) {
        this.service = service;
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<?> send(Authentication auth, @PathVariable Long receiverId) {
        try {
            return ResponseEntity.ok(service.send((Long) auth.getPrincipal(), receiverId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/incoming")
    public ResponseEntity<?> incoming(Authentication auth) {
        return ResponseEntity.ok(service.incoming((Long) auth.getPrincipal()));
    }

    @GetMapping("/outgoing")
    public ResponseEntity<?> outgoing(Authentication auth) {
        return ResponseEntity.ok(service.outgoing((Long) auth.getPrincipal()));
    }

    @GetMapping("/accepted")
    public ResponseEntity<?> accepted(Authentication auth) {
        return ResponseEntity.ok(service.accepted((Long) auth.getPrincipal()));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(Authentication auth, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.accept((Long) auth.getPrincipal(), id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(Authentication auth, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.reject((Long) auth.getPrincipal(), id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}