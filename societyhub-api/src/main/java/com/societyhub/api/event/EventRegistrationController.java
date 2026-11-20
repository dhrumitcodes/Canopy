package com.societyhub.api.event;
import com.societyhub.api.event.dto.RegistrationResponse;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/v1/events") @RequiredArgsConstructor
public class EventRegistrationController {
 private final EventRegistrationService service; private final UserRepository users;
 @PostMapping("/{eventId}/register") public ResponseEntity<RegistrationResponse> register(@PathVariable Long eventId,Authentication a){return ResponseEntity.status(HttpStatus.CREATED).body(service.registerForEvent(eventId,user(a)));}
 @DeleteMapping("/{eventId}/register") public ResponseEntity<Void> cancel(@PathVariable Long eventId,Authentication a){service.cancelRegistration(eventId,user(a));return ResponseEntity.noContent().build();}
 @GetMapping("/{eventId}/attendees") @PreAuthorize("@societySecurity.isSocietyOfficerOrAdmin(@eventService.getEventById(#eventId).societyId)")
 public List<RegistrationResponse> attendees(@PathVariable Long eventId){return service.getEventRegistrations(eventId);}
 @GetMapping("/my-registrations") public List<RegistrationResponse> mine(Authentication a){return service.getUserRegistrations(user(a));}
 @PostMapping("/check-in") @PreAuthorize("hasAnyRole('SOCIETY_ADMIN','COLLEGE_ADMIN','PLATFORM_ADMIN','MEMBER')")
 public RegistrationResponse checkIn(@RequestBody CheckInRequest body){if(body==null||body.qrToken()==null||body.qrToken().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"QR token is required");return service.checkIn(body.qrToken().trim());}
 private User user(Authentication a){return users.findByEmail(a.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found"));}
 public record CheckInRequest(String qrToken){}
}