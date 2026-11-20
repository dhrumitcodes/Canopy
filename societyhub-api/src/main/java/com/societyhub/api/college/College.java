package com.societyhub.api.college;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "colleges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class College {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "email_domain", nullable = false, unique = true)
    private String emailDomain;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}