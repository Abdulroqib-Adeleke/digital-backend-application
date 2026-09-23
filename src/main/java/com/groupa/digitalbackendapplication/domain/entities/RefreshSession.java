package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refresh_sessions")
public class RefreshSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "active_session_id", length = 50)
    private String activeSessionId;

    @Column(name = "hashed_token")
    private String token;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "expiry_time")
    private LocalDateTime expiryDate;

    @Column(name = "created_date")
    @CreationTimestamp
    LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    LocalDateTime updatedDate;
}