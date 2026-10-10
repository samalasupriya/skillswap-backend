package com.skillswap.repository;

import com.skillswap.model.SkillType;
import com.skillswap.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserId(Long userId);
    List<UserSkill> findByUserIdNot(Long userId);
    boolean existsByUserIdAndSkillIdAndType(Long userId, Long skillId, SkillType type);
}