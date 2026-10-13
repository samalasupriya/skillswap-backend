package com.skillswap.controller;

import com.skillswap.dto.ProfileRequest;
import com.skillswap.model.User;
import com.skillswap.model.UserProfile;
import com.skillswap.repository.UserProfileRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "${app.cors.origins}")
public class ProfileController {

    private final UserProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final SkillService skillService;

    public ProfileController(UserProfileRepository profileRepo, UserRepository userRepo,
                             SkillService skillService) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
        this.skillService = skillService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        try {
            return ResponseEntity.ok(build((Long) auth.getPrincipal(), true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // View another person's profile (no email shown)
    @GetMapping("/{userId}")
    public ResponseEntity<?> view(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(build(userId, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> save(Authentication auth, @RequestBody ProfileRequest req) {
        try {
            Long userId = (Long) auth.getPrincipal();
            UserProfile p = profileRepo.findByUserId(userId).orElseGet(() -> {
                UserProfile n = new UserProfile();
                n.setUserId(userId);
                return n;
            });
            p.setHeadline(clean(req.headline(), 120, "Headline"));
            p.setBio(clean(req.bio(), 1000, "Bio"));
            p.setLocation(clean(req.location(), 100, "Location"));
            p.setAvailability(clean(req.availability(), 200, "Availability"));
            profileRepo.save(p);
            return ResponseEntity.ok(build(userId, true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String clean(String s, int max, String field) {
        String v = (s == null) ? "" : s.trim();
        if (v.length() > max) {
            throw new IllegalArgumentException(field + " must be at most " + max + " characters");
        }
        return v;
    }

    private Map<String, Object> build(Long userId, boolean includeEmail) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile p = profileRepo.findByUserId(userId).orElse(null);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", userId);
        m.put("name", user.getName() == null ? "" : user.getName());
        if (includeEmail) m.put("email", user.getEmail());
        m.put("headline", p == null || p.getHeadline() == null ? "" : p.getHeadline());
        m.put("bio", p == null || p.getBio() == null ? "" : p.getBio());
        m.put("location", p == null || p.getLocation() == null ? "" : p.getLocation());
        m.put("availability", p == null || p.getAvailability() == null ? "" : p.getAvailability());
        m.put("skills", skillService.mySkills(userId));
        return m;
    }
}