package com.vivek.clubRegistration.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email", name = "uk_member_email")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Personal Info ──────────────────────────────────────
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Enter a valid phone number (10–15 digits)")
    @Column(nullable = false, length = 20)
    private String phone;

    // ── Age Restriction Rule (must be 18+) ─────────────────
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    // ── Membership Details ─────────────────────────────────
    @NotBlank(message = "Membership type is required")
    @Column(nullable = false, length = 20)
    private String membershipType; // SILVER, GOLD, PLATINUM

    @Column(length = 255)
    private String address;

    // ── Status & Admin Fields ──────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    private LocalDateTime approvedAt;

    @Column(length = 500)
    private String adminNote;

    // ── Computed Helper ────────────────────────────────────
    @Transient
    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
