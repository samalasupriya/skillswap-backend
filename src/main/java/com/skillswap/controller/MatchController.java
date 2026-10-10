package com.skillswap.controller;

import com.skillswap.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:5173")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<?> matches(Authentication auth) {
        return ResponseEntity.ok(matchService.findMatches((Long) auth.getPrincipal()));
    }
}