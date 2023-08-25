package team6.sobun.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team6.sobun.domain.notification.entity.Notification;
import team6.sobun.domain.user.entity.User;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long memberId);

    @Query("SELECT n FROM notification n WHERE n.receiver = :user")
    List<Notification> findAllByUser(@Param("user") User user);

    @Query("Select n FROM notification n WHERE n.isRead = :read AND n.receiver = :user")
    List<Notification> findAllByUnread(@Param("read") boolean read, @Param("user") User user);
}
