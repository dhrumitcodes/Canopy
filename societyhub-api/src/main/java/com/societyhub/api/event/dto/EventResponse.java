package com.societyhub.api.event.dto;
import com.societyhub.api.event.Event;
import com.societyhub.api.event.EventStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data @Builder
public class EventResponse {
    private Long id, societyId;
    private String societyName, title, description, location, reviewNote;
    private Integer capacity;
    private long registeredCount;
    private boolean registered;
    private LocalDateTime startTime, endTime, createdAt, reviewedAt;
    private EventStatus status;
    public static EventResponse fromEntity(Event e, long count, boolean registered) {
        return EventResponse.builder().id(e.getId()).societyId(e.getSociety().getId()).societyName(e.getSociety().getName())
          .title(e.getTitle()).description(e.getDescription()).location(e.getLocation()).capacity(e.getCapacity())
          .registeredCount(count).registered(registered).startTime(e.getStartTime()).endTime(e.getEndTime())
          .createdAt(e.getCreatedAt()).status(e.getStatus()).reviewNote(e.getReviewNote()).reviewedAt(e.getReviewedAt()).build();
    }
    public static EventResponse fromEntity(Event e) { return fromEntity(e, 0, false); }
}