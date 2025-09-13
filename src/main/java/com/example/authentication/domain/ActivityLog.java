package com.example.authentication.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_user_time", columnList = "user_id, occurredAt")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 64)
    private String type; // LOGIN, LOGOUT, REGISTER, REFRESH, PASSWORD_CHANGE, ROLE_CHANGE

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(length = 255)
    private String message;
}


