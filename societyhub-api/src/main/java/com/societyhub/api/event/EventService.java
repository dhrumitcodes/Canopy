package com.societyhub.api.event;
import com.societyhub.api.event.dto.EventRequest;
import com.societyhub.api.event.dto.EventResponse;
import com.societyhub.api.notification.NotificationService;
import com.societyhub.api.society.Society;
import com.societyhub.api.society.SocietyRepository;
import com.societyhub.api.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
@Service @RequiredArgsConstructor
public class EventService {
 private final EventRepository eventRepository; private final SocietyRepository societyRepository;
 private final EventRegistrationRepository registrationRepository; private final UserRepository userRepository;
 private final NotificationService notificationService;
 @Transactional public EventResponse createEvent(Long societyId,EventRequest request){
  Society society=societyRepository.findById(societyId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Society not found"));
  validateTimes(request); User actor=currentUser();
  Event event=Event.builder().society(society).title(request.getTitle()).description(request.getDescription()).location(request.getLocation())
   .startTime(request.getStartTime()).endTime(request.getEndTime()).createdBy(actor).status(EventStatus.PENDING).build();
  if(request.getCapacity()!=null)event.setCapacity(request.getCapacity());
  Event saved=eventRepository.save(event);
  if(society.getCollege()!=null) userRepository.findByCollegeIdAndRole(society.getCollege().getId(),Role.COLLEGE_ADMIN).forEach(admin->
   notificationService.notifyUser(admin,"EVENT_REVIEW","Event awaiting approval",saved.getTitle()+" was submitted for review.",saved));
  return toResponse(saved,actor==null?null:actor.getId());
 }
 @Transactional(readOnly=true) public Page<EventResponse> getFilteredEvents(Long societyId,String search,String location,Boolean upcoming,Pageable pageable){
  User user=currentUser(); Long collegeId=user==null||user.getCollege()==null?null:user.getCollege().getId();
  Specification<Event> spec=Specification.where(EventSpecification.hasSocietyId(societyId))
   .and(EventSpecification.hasCollegeId(user!=null&&user.getRole()==Role.PLATFORM_ADMIN?null:collegeId))
   .and(EventSpecification.titleContains(search)).and(EventSpecification.locationContains(location)).and(EventSpecification.isUpcoming(upcoming));
  if(user!=null&&user.getRole()==Role.PLATFORM_ADMIN)spec=Specification.where(EventSpecification.hasSocietyId(societyId)).and(EventSpecification.titleContains(search)).and(EventSpecification.locationContains(location)).and(EventSpecification.isUpcoming(upcoming));
  if(user==null||(user.getRole()!=Role.COLLEGE_ADMIN&&user.getRole()!=Role.PLATFORM_ADMIN)) {
   Long viewerId=user==null?null:user.getId();
   spec=spec.and((root,q,cb)->cb.or(cb.equal(root.get("status"),EventStatus.APPROVED),viewerId==null?cb.disjunction():cb.equal(root.get("createdBy").get("id"),viewerId)));
  }
  return eventRepository.findAll(spec,pageable).map(e->toResponse(e,user==null?null:user.getId()));
 }
 @Transactional(readOnly=true) public EventResponse getEventById(Long eventId){
  Event e=eventRepository.findById(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
  User u=currentUser(); boolean registered=u!=null&&registrationRepository.existsByEventIdAndUserId(eventId,u.getId());
  if(e.getStatus()!=EventStatus.APPROVED && (u==null||!canReview(u,e)&&!u.getId().equals(e.getCreatedBy()==null?null:e.getCreatedBy().getId())&&!registered)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
  return toResponse(e,u==null?null:u.getId());
 }
 @Transactional public EventResponse updateEvent(Long eventId,EventRequest request){
  Event e=eventRepository.findByIdForUpdate(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
  validateTimes(request); if(request.getCapacity()!=null){long count=registrationRepository.countByEventId(eventId);if(request.getCapacity()<count)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Capacity cannot be lower than registered attendees");e.setCapacity(request.getCapacity());}
  e.setTitle(request.getTitle());e.setDescription(request.getDescription());e.setLocation(request.getLocation());e.setStartTime(request.getStartTime());e.setEndTime(request.getEndTime());
  e.setStatus(EventStatus.PENDING);e.setReviewNote(null);e.setReviewedAt(null);e.setReviewedBy(null);
  return toResponse(eventRepository.save(e),currentUserId());
 }
 @Transactional public void deleteEvent(Long eventId){
  Event e=eventRepository.findByIdForUpdate(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
  if(e.getStatus()==EventStatus.CANCELLED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Event is already cancelled");
  e.setStatus(EventStatus.CANCELLED);
  registrationRepository.findByEventId(eventId).forEach(r->notificationService.notifyUser(r.getUser(),"EVENT_CANCELLED","Event cancelled",e.getTitle()+" has been cancelled by the organizer.",e));
 }
 @Transactional(readOnly=true) public List<EventResponse> pendingForCollege(Long collegeId){return eventRepository.findPendingByCollege(collegeId).stream().map(e->toResponse(e,null)).toList();}
 @Transactional public EventResponse review(Long eventId,boolean approve,String note){
  User reviewer=currentUser(); Event e=eventRepository.findByIdForUpdate(eventId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));
  if(!canReview(reviewer,e))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only review events for your college");
  if(e.getStatus()!=EventStatus.PENDING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Only pending events can be reviewed");
  if(!approve&&(note==null||note.isBlank()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A rejection reason is required");
  e.setStatus(approve?EventStatus.APPROVED:EventStatus.REJECTED);e.setReviewNote(note==null?null:note.trim());e.setReviewedAt(LocalDateTime.now());e.setReviewedBy(reviewer);
  Event saved=eventRepository.save(e);
  if(saved.getCreatedBy()!=null)notificationService.notifyUser(saved.getCreatedBy(),"EVENT_REVIEW_RESULT",approve?"Event approved":"Event rejected",
   approve?"Your event "+saved.getTitle()+" has been approved.":"Your event "+saved.getTitle()+" was rejected: "+saved.getReviewNote(),saved);
  return toResponse(saved,reviewer.getId());
 }
 private boolean canReview(User user,Event event){return user!=null&&(user.getRole()==Role.PLATFORM_ADMIN||user.getRole()==Role.COLLEGE_ADMIN&&user.getCollege()!=null&&user.getCollege().getId().equals(event.getSociety().getCollege().getId()));}
 private void validateTimes(EventRequest r){if(!r.getEndTime().isAfter(r.getStartTime()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"End time must be after start time");}
 private EventResponse toResponse(Event e,Long uid){return EventResponse.fromEntity(e,registrationRepository.countByEventId(e.getId()),uid!=null&&registrationRepository.existsByEventIdAndUserId(e.getId(),uid));}
 private Long currentUserId(){User u=currentUser();return u==null?null:u.getId();}
 private User currentUser(){Authentication a=SecurityContextHolder.getContext().getAuthentication();if(a==null||!a.isAuthenticated()||"anonymousUser".equals(a.getPrincipal()))return null;return userRepository.findByEmail(a.getName()).orElse(null);}
}