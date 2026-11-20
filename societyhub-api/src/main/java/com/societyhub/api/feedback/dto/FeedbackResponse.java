package com.societyhub.api.feedback.dto;
import com.societyhub.api.feedback.EventFeedback; import java.time.LocalDateTime;
public record FeedbackResponse(Long id,Long eventId,String eventTitle,String userFullName,Integer rating,String comments,LocalDateTime createdAt){
public static FeedbackResponse from(EventFeedback f){return new FeedbackResponse(f.getId(),f.getEvent().getId(),f.getEvent().getTitle(),f.getUser().getFullName(),f.getRating(),f.getComments(),f.getCreatedAt());}
}