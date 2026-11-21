package com.societyhub.api.feedback;

import com.societyhub.api.event.*;
import com.societyhub.api.feedback.dto.FeedbackRequest;
import com.societyhub.api.security.SocietySecurityEvaluator;
import com.societyhub.api.society.Society;
import com.societyhub.api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventFeedbackServiceTest {
 @Mock EventRepository events;
 @Mock EventRegistrationRepository registrations;
 @Mock EventFeedbackRepository feedback;
 @Mock SocietySecurityEvaluator security;
 @InjectMocks EventFeedbackService service;
 private Event event(LocalDateTime end) {
  Society society=Society.builder().id(8L).name("Robotics Club").build();
  return Event.builder().id(4L).title("Design Sprint").society(society).status(EventStatus.APPROVED).endTime(end).build();
 }
 private User user(){return User.builder().id(12L).fullName("Attendee").email("a@college.edu").build();}
 private FeedbackRequest request(){return new FeedbackRequest(5,"Great event");}
 @Test void rejectsFeedbackBeforeEventEnds(){
  Event e=event(LocalDateTime.now().plusHours(1)); when(events.findById(4L)).thenReturn(Optional.of(e));
  ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->service.submit(4L,user(),request()));
  assertEquals(409,ex.getStatusCode().value()); verifyNoInteractions(registrations,feedback);
 }
 @Test void requiresARegistrationAndVerifiedCheckIn(){
  Event e=event(LocalDateTime.now().minusMinutes(1)); User u=user(); when(events.findById(4L)).thenReturn(Optional.of(e));
  when(registrations.findByEventIdAndUserId(4L,12L)).thenReturn(Optional.of(EventRegistration.builder().id(2L).event(e).user(u).build()));
  ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->service.submit(4L,u,request()));
  assertEquals(403,ex.getStatusCode().value()); verifyNoInteractions(feedback);
 }
 @Test void savesFeedbackForARegisteredAttendeeWhoCheckedIn(){
  Event e=event(LocalDateTime.now().minusMinutes(1)); User u=user(); when(events.findById(4L)).thenReturn(Optional.of(e));
  when(registrations.findByEventIdAndUserId(4L,12L)).thenReturn(Optional.of(EventRegistration.builder().id(2L).event(e).user(u).checkedInAt(LocalDateTime.now().minusHours(1)).build()));
  when(feedback.findByEventIdAndUserId(4L,12L)).thenReturn(Optional.empty());
  when(feedback.save(any(EventFeedback.class))).thenAnswer(inv->inv.getArgument(0));
  var response=service.submit(4L,u,request());
  assertEquals(4L,response.eventId()); assertEquals("Design Sprint",response.eventTitle());
  assertEquals("Attendee",response.userFullName()); assertEquals(5,response.rating()); assertEquals("Great event",response.comments());
 }
 @Test void organizersCannotReadFeedbackForEventsTheyDoNotManage(){
  Event e=event(LocalDateTime.now().minusDays(1)); when(events.findById(4L)).thenReturn(Optional.of(e));
  when(security.isSocietyOfficerOrAdmin(8L)).thenReturn(false);
  ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->service.forOrganizers(4L));
  assertEquals(403,ex.getStatusCode().value()); verify(feedback,never()).findByEventIdOrderByCreatedAtDesc(4L);
 }
 @Test void authorizedOrganizersCanReadEventFeedback(){
  Event e=event(LocalDateTime.now().minusDays(1)); when(events.findById(4L)).thenReturn(Optional.of(e));
  when(security.isSocietyOfficerOrAdmin(8L)).thenReturn(true);
  when(feedback.findByEventIdOrderByCreatedAtDesc(4L)).thenReturn(List.of());
  assertTrue(service.forOrganizers(4L).isEmpty());
 }
}
