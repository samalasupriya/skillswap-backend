package com.skillswap.controller;

import com.skillswap.dto.AddSkillRequest;
import com.skillswap.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "${app.cors.origins}")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(skillService.allSkills());
    }

    @PostMapping("/my")
    public ResponseEntity<?> add(Authentication auth, @RequestBody AddSkillRequest req) {
        try {
            return ResponseEntity.ok(skillService.addMySkill((Long) auth.getPrincipal(), req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> mine(Authentication auth) {
        return ResponseEntity.ok(skillService.mySkills((Long) auth.getPrincipal()));
    }

    @DeleteMapping("/my/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        try {
            skillService.deleteMySkill((Long) auth.getPrincipal(), id);
            return ResponseEntity.ok(Map.of("deleted", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/browse")
    public ResponseEntity<?> browse(Authentication auth) {
        return ResponseEntity.ok(skillService.browse((Long) auth.getPrincipal()));
    }
}