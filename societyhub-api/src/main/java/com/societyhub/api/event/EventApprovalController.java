package com.societyhub.api.event;
import com.societyhub.api.event.dto.EventResponse;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequestMapping("/api/v1/college/events") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COLLEGE_ADMIN','PLATFORM_ADMIN')")
public class EventApprovalController {
 private final EventService events; private final UserRepository users;
 private User actor(Authentication a){return users.findByEmail(a.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 @GetMapping("/pending") public List<EventResponse> pending(Authentication a){User u=actor(a);if(u.getRole()==com.societyhub.api.user.Role.PLATFORM_ADMIN)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Platform-wide review queue is not supported");return events.pendingForCollege(u.getCollege().getId());}
 @PostMapping("/{eventId}/approve") public EventResponse approve(@PathVariable Long eventId,Authentication a){actor(a);return events.review(eventId,true,null);}
 @PostMapping("/{eventId}/reject") public EventResponse reject(@PathVariable Long eventId,@RequestBody ReviewRequest body,Authentication a){actor(a);return events.review(eventId,false,body.reason());}
 public record ReviewRequest(String reason){}
}