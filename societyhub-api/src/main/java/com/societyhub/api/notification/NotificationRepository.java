package com.societyhub.api.notification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface NotificationRepository extends JpaRepository<Notification,Long> {
 List<Notification> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);
 Optional<Notification> findByIdAndUserId(Long id,Long userId);
 long countByUserIdAndReadAtIsNull(Long userId);
 @Modifying @Query("update Notification n set n.readAt=:now where n.user.id=:userId and n.readAt is null")
 int markAllRead(@Param("userId") Long userId,@Param("now") LocalDateTime now);
}