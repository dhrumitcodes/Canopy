package com.societyhub.api.feedback;
import com.societyhub.api.event.Event; import com.societyhub.api.user.User; import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="event_feedback",uniqueConstraints=@UniqueConstraint(columnNames={"event_id","user_id"})) @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventFeedback {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="event_id",nullable=false) private Event event;
@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
@Column(nullable=false) private Integer rating; @Column(length=2000) private String comments;
@Column(name="created_at",nullable=false,updatable=false) @Builder.Default private LocalDateTime createdAt=LocalDateTime.now();
@Column(name="updated_at",nullable=false) @Builder.Default private LocalDateTime updatedAt=LocalDateTime.now();
}