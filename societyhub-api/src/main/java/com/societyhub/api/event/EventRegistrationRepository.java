package com.societyhub.api.event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface EventRegistrationRepository extends JpaRepository<EventRegistration,Long>{
 boolean existsByEventIdAndUserId(Long eventId,Long userId); long countByEventId(Long eventId);
 Optional<EventRegistration> findByEventIdAndUserId(Long eventId,Long userId);
 List<EventRegistration> findByUserId(Long userId); List<EventRegistration> findByEventId(Long eventId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from EventRegistration r where r.qrToken=:token") Optional<EventRegistration> findByQrTokenForUpdate(@Param("token") String token);
}