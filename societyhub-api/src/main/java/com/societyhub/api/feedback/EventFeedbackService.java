package com.societyhub.api.feedback;
import com.societyhub.api.event.*; import com.societyhub.api.feedback.dto.*; import com.societyhub.api.security.SocietySecurityEvaluator; import com.societyhub.api.user.User; import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException; import java.time.LocalDateTime; import java.util.List;
@Service @RequiredArgsConstructor
public class EventFeedbackService{
private final EventRepository events; private final EventRegistrationRepository registrations; private final EventFeedbackRepository feedback; private final SocietySecurityEvaluator security;
@Transactional public FeedbackResponse submit(Long eventId,User user,FeedbackRequest request){
Event e=events.findById(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
if(e.getStatus()!=EventStatus.APPROVED||e.getEndTime().isAfter(LocalDateTime.now()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Feedback opens after the event has ended");
EventRegistration reg=registrations.findByEventIdAndUserId(eventId,user.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"Only registered attendees can leave feedback"));
if(reg.getCheckedInAt()==null)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Feedback is available only to attendees who checked in");
EventFeedback f=feedback.findByEventIdAndUserId(eventId,user.getId()).orElseGet(()->EventFeedback.builder().event(e).user(user).build());
f.setRating(request.rating()); f.setComments(request.comments()==null?null:request.comments().trim()); f.setUpdatedAt(LocalDateTime.now()); return FeedbackResponse.from(feedback.save(f));
}
@Transactional(readOnly=true) public List<FeedbackResponse> forOrganizers(Long eventId){
Event e=events.findById(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
if(!security.isSocietyOfficerOrAdmin(e.getSociety().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You cannot view feedback for this event");
return feedback.findByEventIdOrderByCreatedAtDesc(eventId).stream().map(FeedbackResponse::from).toList();
}
}