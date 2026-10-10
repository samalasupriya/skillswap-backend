package com.skillswap.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    private String headline;

    @Column(length = 1000)
    private String bio;

    private String location;

    @Column(length = 200)
    private String availability;
}