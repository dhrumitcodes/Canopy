package com.societyhub.api.event;
import com.societyhub.api.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "event_registrations", uniqueConstraints = {@UniqueConstraint(columnNames = {"event_id", "user_id"})})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventRegistration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false) private Event event;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "registered_at", updatable = false) @Builder.Default private LocalDateTime registeredAt = LocalDateTime.now();
    @Column(name = "qr_token", nullable = false, unique = true) @Builder.Default private String qrToken = UUID.randomUUID().toString();
    @Column(name = "checked_in_at") private LocalDateTime checkedInAt;
}