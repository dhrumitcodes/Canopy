package com.societyhub.api.event;
import com.societyhub.api.event.dto.RegistrationResponse;
import com.societyhub.api.notification.NotificationService;
import com.societyhub.api.security.SocietySecurityEvaluator;
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
 private final EventRegistrationRepository registrations; private final EventRepository events; private final NotificationService notifications; private final SocietySecurityEvaluator security;
 @Transactional public RegistrationResponse registerForEvent(Long eventId,User user){
  Event e=events.findByIdForUpdate(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
  if(e.getStatus()!=EventStatus.APPROVED)throw new ResponseStatusException(HttpStatus.CONFLICT,"This event is not open for registration");
  if(user.getCollege()==null||!user.getCollege().getId().equals(e.getSociety().getCollege().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only register for events hosted by your college");
  if(e.getEndTime().isBefore(LocalDateTime.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot register for past events");
  if(registrations.existsByEventIdAndUserId(eventId,user.getId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"You are already registered for this event");
  if(registrations.countByEventId(eventId)>=e.getCapacity())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Event is fully booked");
  EventRegistration r=registrations.save(EventRegistration.builder().event(e).user(user).build());
  notifications.notifyUser(user,"REGISTRATION_CONFIRMED","Registration confirmed","You are registered for "+e.getTitle()+". Your QR ticket is ready.",e);return RegistrationResponse.fromEntity(r);
 }
 @Transactional public void cancelRegistration(Long id,User user){EventRegistration r=registrations.findByEventIdAndUserId(id,user.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"You are not registered for this event"));registrations.delete(r);}
 @Transactional(readOnly=true) public List<RegistrationResponse> getEventRegistrations(Long id){if(!events.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");return registrations.findByEventId(id).stream().map(RegistrationResponse::fromEntity).toList();}
 @Transactional(readOnly=true) public List<RegistrationResponse> getUserRegistrations(User u){return registrations.findByUserId(u.getId()).stream().map(RegistrationResponse::fromEntity).toList();}
 @Transactional public RegistrationResponse checkIn(String token,User actor){
  EventRegistration r=registrations.findByQrTokenForUpdate(token).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Invalid ticket"));
  if(!security.isSocietyOfficerOrAdmin(r.getEvent().getSociety().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You cannot check in attendees for this society");
  if(r.getEvent().getStatus()!=EventStatus.APPROVED)throw new ResponseStatusException(HttpStatus.CONFLICT,"This event is not active for check-in");
  LocalDateTime now=LocalDateTime.now();if(now.isBefore(r.getEvent().getStartTime())||now.isAfter(r.getEvent().getEndTime()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Check-in is only available during the event");
  if(r.getCheckedInAt()!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"This ticket has already been checked in");
  r.setCheckedInAt(now);return RegistrationResponse.fromEntity(r);
 }
}