package com.skillswap.dto;

public record ReviewRequest(Long sessionId, int rating, String comment) {}