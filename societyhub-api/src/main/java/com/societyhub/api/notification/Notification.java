package com.societyhub.api.notification;
import com.societyhub.api.user.User;
import com.societyhub.api.event.Event;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="notifications") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id") private User user;
 @Column(nullable=false,length=40) private String type;
 @Column(nullable=false,length=180) private String title;
 @Column(nullable=false,columnDefinition="TEXT") private String message;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="related_event_id") private Event relatedEvent;
 @Column(name="read_at") private LocalDateTime readAt;
 @Column(name="created_at",nullable=false,updatable=false) @Builder.Default private LocalDateTime createdAt=LocalDateTime.now();
}