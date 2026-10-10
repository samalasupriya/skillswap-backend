
package com.skillswap.dto;

import java.time.LocalDateTime;

public record ProposeSessionRequest(Long connectionId, String topic,
                                    LocalDateTime scheduledAt, String meetingLink) {}