package com.societyhub.api.event.dto;
import com.societyhub.api.event.EventRegistration;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data @Builder
public class RegistrationResponse {
    private Long id, eventId, societyId, userId;
    private String eventTitle, societyName, userEmail, userFullName, qrToken;
    private LocalDateTime eventStartTime, eventEndTime, checkedInAt;
    public static RegistrationResponse fromEntity(EventRegistration r) {
        return RegistrationResponse.builder().id(r.getId()).eventId(r.getEvent().getId()).eventTitle(r.getEvent().getTitle())
          .societyId(r.getEvent().getSociety().getId()).societyName(r.getEvent().getSociety().getName())
          .eventStartTime(r.getEvent().getStartTime()).eventEndTime(r.getEvent().getEndTime()).userId(r.getUser().getId())
          .userEmail(r.getUser().getEmail()).userFullName(r.getUser().getFullName()).qrToken(r.getQrToken()).checkedInAt(r.getCheckedInAt()).build();
    }
}