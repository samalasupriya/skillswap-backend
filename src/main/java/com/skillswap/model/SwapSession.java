package com.skillswap.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "swap_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SwapSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "connection_id")
    private ConnectionRequest connection;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proposed_by")
    private User proposedBy;

    private String topic;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private String meetingLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.PROPOSED;
}