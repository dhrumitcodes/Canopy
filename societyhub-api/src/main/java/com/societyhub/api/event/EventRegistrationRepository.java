package com.societyhub.api.event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    long countByEventId(Long eventId);
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
    List<EventRegistration> findByUserId(Long userId);
    List<EventRegistration> findByEventId(Long eventId);
    Optional<EventRegistration> findByQrToken(String qrToken);
}