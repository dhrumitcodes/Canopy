package com.societyhub.api.notification;
import com.societyhub.api.event.Event;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
@Service @RequiredArgsConstructor
public class NotificationService {
 private final NotificationRepository notifications;
 private final UserRepository users;
 @Transactional public void notifyUser(User user,String type,String title,String message,Event event) {
  notifications.save(Notification.builder().user(user).type(type).title(title).message(message).relatedEvent(event).build());
 }
 @Transactional(readOnly=true) public Map<String,Object> list(Long userId) {
  return Map.of("items",notifications.findTop100ByUserIdOrderByCreatedAtDesc(userId),"unreadCount",notifications.countByUserIdAndReadAtIsNull(userId));
 }
 @Transactional public void markRead(Long id,Long userId) {
  Notification n=notifications.findByIdAndUserId(id,userId).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Notification not found"));
  if(n.getReadAt()==null)n.setReadAt(LocalDateTime.now());
 }
 @Transactional public void markAllRead(Long userId) {
  notifications.findTop100ByUserIdOrderByCreatedAtDesc(userId).stream().filter(n->n.getReadAt()==null).forEach(n->n.setReadAt(LocalDateTime.now()));
 }
}