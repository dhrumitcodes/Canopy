package com.societyhub.api.event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface EventRepository extends JpaRepository<Event,Long>,JpaSpecificationExecutor<Event>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select e from Event e where e.id=:eventId") Optional<Event> findByIdForUpdate(@Param("eventId") Long eventId);
 List<Event> findByStatusAndStartTimeBetweenAndReminderSentAtIsNull(EventStatus status, java.time.LocalDateTime from, java.time.LocalDateTime to);
 @Query("select e from Event e where e.status = com.societyhub.api.event.EventStatus.PENDING and e.society.college.id=:collegeId order by e.createdAt asc") List<Event> findPendingByCollege(@Param("collegeId") Long collegeId);
}