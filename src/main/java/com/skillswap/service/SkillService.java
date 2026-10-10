package com.skillswap.service;

import com.skillswap.dto.AddSkillRequest;
import com.skillswap.model.*;
import com.skillswap.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SkillService {

    private final SkillRepository skillRepo;
    private final UserSkillRepository userSkillRepo;
    private final UserRepository userRepo;

    public SkillService(SkillRepository skillRepo, UserSkillRepository userSkillRepo, UserRepository userRepo) {
        this.skillRepo = skillRepo;
        this.userSkillRepo = userSkillRepo;
        this.userRepo = userRepo;
    }

    public List<Skill> allSkills() {
        return skillRepo.findAll();
    }

    public Map<String, Object> addMySkill(Long userId, AddSkillRequest req) {
        if (req.skillName() == null || req.skillName().isBlank() || req.type() == null) {
            throw new IllegalArgumentException("skillName and type (TEACH or LEARN) are required");
        }
        String name = req.skillName().trim();

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Skill skill = skillRepo.findByNameIgnoreCase(name).orElseGet(() -> {
            Skill s = new Skill();
            s.setName(name);
            return skillRepo.save(s);
        });

        if (userSkillRepo.existsByUserIdAndSkillIdAndType(userId, skill.getId(), req.type())) {
            throw new IllegalArgumentException("You already added this skill as " + req.type());
        }

        UserSkill us = new UserSkill();
        us.setUser(user);
        us.setSkill(skill);
        us.setType(req.type());
        us = userSkillRepo.save(us);

        return Map.of("id", us.getId(), "skillName", skill.getName(), "type", us.getType());
    }

    public List<Map<String, Object>> mySkills(Long userId) {
        return userSkillRepo.findByUserId(userId).stream()
                .map(us -> Map.<String, Object>of(
                        "id", us.getId(),
                        "skillName", us.getSkill().getName(),
                        "type", us.getType()))
                .toList();
    }

    public void deleteMySkill(Long userId, Long userSkillId) {
        UserSkill us = userSkillRepo.findById(userSkillId)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        if (!us.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not your skill");
        }
        userSkillRepo.delete(us);
    }

    // Everyone except me
    public List<Map<String, Object>> browse(Long myId) {
        return userSkillRepo.findByUserIdNot(myId).stream()
                .map(us -> Map.<String, Object>of(
                        "userId", us.getUser().getId(),
                        "userName", us.getUser().getName() == null ? "" : us.getUser().getName(),
                        "skillName", us.getSkill().getName(),
                        "type", us.getType()))
                .toList();
    }
}