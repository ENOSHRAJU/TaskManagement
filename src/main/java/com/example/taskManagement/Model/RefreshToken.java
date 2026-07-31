package com.example.taskManagement.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Stores refresh token details for authenticated users")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(hidden = true)
    private UUID id;

    @Column(nullable = false, unique = true, length = 36)
    @Schema(description = "Unique refresh token")
    private String token;

    @Column(nullable = false)
    @Schema(description = "Refresh token expiry date and time")
    private LocalDateTime expiryDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable =false, unique = true)
    @Schema(hidden = true)
    private User user;

    @Column(nullable = false, updatable = false)
    @Schema(hidden = true)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}