package com.skillswap.dto;

import com.skillswap.model.SkillType;

public record AddSkillRequest(String skillName, SkillType type) {}