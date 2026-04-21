package OMS_backend.repository;

import OMS_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_EmailOrderByCreatedAtDesc(String email);

    long countByUser_EmailAndReadStatusFalse(String email);
}
