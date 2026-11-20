package com.societyhub.api.feedback;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface EventFeedbackRepository extends JpaRepository<EventFeedback,Long>{
Optional<EventFeedback> findByEventIdAndUserId(Long eventId,Long userId);
List<EventFeedback> findByEventIdOrderByCreatedAtDesc(Long eventId);
}