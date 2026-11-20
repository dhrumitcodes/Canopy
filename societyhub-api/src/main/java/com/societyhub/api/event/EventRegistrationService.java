package com.societyhub.api.event;
import com.societyhub.api.event.dto.RegistrationResponse;
import com.societyhub.api.notification.NotificationService;
import com.societyhub.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
@Service @RequiredArgsConstructor
public class EventRegistrationService {
 private final EventRegistrationRepository registrations; private final EventRepository events; private final NotificationService notifications;
 @Transactional public RegistrationResponse registerForEvent(Long eventId,User user){
  Event event=events.findByIdForUpdate(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
  if(event.getStatus()!=EventStatus.APPROVED)throw new ResponseStatusException(HttpStatus.CONFLICT,"This event is not open for registration");
  if(user.getCollege()==null||!user.getCollege().getId().equals(event.getSociety().getCollege().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only register for events hosted by your college");
  if(event.getEndTime().isBefore(LocalDateTime.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot register for past events");
  if(registrations.existsByEventIdAndUserId(eventId,user.getId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"You are already registered for this event");
  if(registrations.countByEventId(eventId)>=event.getCapacity())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Event is fully booked");
  EventRegistration saved=registrations.save(EventRegistration.builder().event(event).user(user).build());
  notifications.notifyUser(user,"REGISTRATION_CONFIRMED","Registration confirmed","You are registered for "+event.getTitle()+". Your QR ticket is ready.",event);
  return RegistrationResponse.fromEntity(saved);
 }
 @Transactional public void cancelRegistration(Long eventId,User user){
  EventRegistration r=registrations.findByEventIdAndUserId(eventId,user.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"You are not registered for this event"));
  registrations.delete(r);
 }
 @Transactional(readOnly=true) public List<RegistrationResponse> getEventRegistrations(Long eventId){
  if(!events.existsById(eventId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
  return registrations.findByEventId(eventId).stream().map(RegistrationResponse::fromEntity).toList();
 }
 @Transactional(readOnly=true) public List<RegistrationResponse> getUserRegistrations(User user){return registrations.findByUserId(user.getId()).stream().map(RegistrationResponse::fromEntity).toList();}
 @Transactional public RegistrationResponse checkIn(String token){
  EventRegistration r=registrations.findByQrTokenForUpdate(token).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Invalid ticket"));
  LocalDateTime now=LocalDateTime.now();
  if(now.isBefore(r.getEvent().getStartTime())||now.isAfter(r.getEvent().getEndTime()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Check-in is only available during the event");
  if(r.getCheckedInAt()!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"This ticket has already been checked in");
  r.setCheckedInAt(now);return RegistrationResponse.fromEntity(r);
 }
}