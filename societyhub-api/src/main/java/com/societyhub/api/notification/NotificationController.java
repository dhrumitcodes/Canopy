package com.societyhub.api.notification;
import com.societyhub.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;
@RestController @RequestMapping("/api/v1/notifications") @RequiredArgsConstructor
public class NotificationController {
 private final NotificationService service; private final UserRepository users;
 private Long userId(Authentication a){return users.findByEmail(a.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();}
 @GetMapping public Map<String,Object> list(Authentication a){return service.list(userId(a));}
 @PatchMapping("/{id}/read") public ResponseEntity<Void> read(@PathVariable Long id,Authentication a){service.markRead(id,userId(a));return ResponseEntity.noContent().build();}
 @PostMapping("/read-all") public ResponseEntity<Void> readAll(Authentication a){service.markAllRead(userId(a));return ResponseEntity.noContent().build();}
}