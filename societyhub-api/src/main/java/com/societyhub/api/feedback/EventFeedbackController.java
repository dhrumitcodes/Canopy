package com.societyhub.api.feedback;
import com.societyhub.api.feedback.dto.*; import com.societyhub.api.user.*; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import java.util.List;
@RestController @RequestMapping("/api/v1/events") @RequiredArgsConstructor
public class EventFeedbackController{
private final EventFeedbackService service; private final UserRepository users;
@PostMapping("/{eventId}/feedback") public FeedbackResponse submit(@PathVariable Long eventId,@Valid @RequestBody FeedbackRequest body,Authentication auth){return service.submit(eventId,user(auth),body);}
@GetMapping("/{eventId}/feedback") public List<FeedbackResponse> list(@PathVariable Long eventId){return service.forOrganizers(eventId);}
private User user(Authentication a){return users.findByEmail(a.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found"));}
}