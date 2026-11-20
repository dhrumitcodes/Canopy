package com.societyhub.api.notification;
import com.societyhub.api.event.Event;
import com.societyhub.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.*;
@Service @RequiredArgsConstructor
public class NotificationService {
 private final NotificationRepository repo;
 @Transactional public void notifyUser(User user,String type,String title,String message,Event event){repo.save(Notification.builder().user(user).type(type).title(title).message(message).relatedEvent(event).build());}
 @Transactional(readOnly=true) public Map<String,Object> list(Long userId){
  List<NotificationItem> items=repo.findTop100ByUserIdOrderByCreatedAtDesc(userId).stream().map(n->new NotificationItem(n.getId(),n.getType(),n.getTitle(),n.getMessage(),n.getRelatedEvent()==null?null:n.getRelatedEvent().getId(),n.getReadAt(),n.getCreatedAt())).toList();
  return Map.of("items",items,"unreadCount",repo.countByUserIdAndReadAtIsNull(userId));
 }
 @Transactional public void markRead(Long id,Long userId){Notification n=repo.findByIdAndUserId(id,userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));if(n.getReadAt()==null)n.setReadAt(LocalDateTime.now());}
 @Transactional public void markAllRead(Long userId){repo.markAllRead(userId,LocalDateTime.now());}
 public record NotificationItem(Long id,String type,String title,String message,Long eventId,LocalDateTime readAt,LocalDateTime createdAt){}
}