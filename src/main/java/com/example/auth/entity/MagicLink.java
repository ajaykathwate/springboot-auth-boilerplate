package com.example.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "magic_links",
    indexes = {
      @Index(name = "idx_token", columnList = "token"),
      @Index(name = "idx_email", columnList = "email")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MagicLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Column(nullable = false, length = 255)
  private String email;

  @NotBlank(message = "Token is required")
  @Column(nullable = false, unique = true, length = 255)
  private String token;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean used = false;

  @Column(name = "user_id")
  private Long userId;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !used && !isExpired();
  }
}
