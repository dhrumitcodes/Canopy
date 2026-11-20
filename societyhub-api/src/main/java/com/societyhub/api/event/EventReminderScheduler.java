package com.societyhub.api.event;
import com.societyhub.api.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Component @RequiredArgsConstructor
public class EventReminderScheduler {
 private final EventRepository events; private final EventRegistrationRepository registrations; private final NotificationService notifications;
 @Scheduled(fixedDelayString="${app.notifications.reminder-check-ms:900000}")
 @Transactional
 public void sendUpcomingReminders(){
  LocalDateTime now=LocalDateTime.now();
  for(Event event:events.findByStatusAndStartTimeBetweenAndReminderSentAtIsNull(EventStatus.APPROVED,now,now.plusHours(24))){
   registrations.findByEventId(event.getId()).forEach(registration->notifications.notifyUser(registration.getUser(),"EVENT_REMINDER","Event reminder",""+event.getTitle()+" starts on "+event.getStartTime()+". Remember to bring your QR ticket.",event));
   event.setReminderSentAt(now);
  }
 }
}