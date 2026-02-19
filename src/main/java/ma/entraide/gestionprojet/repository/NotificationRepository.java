package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByDateCreationDesc(Long userId, Pageable pageable);

    long countByUserIdAndLueFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.user.id = :userId AND n.lue = false")
    void marquerToutesCommeLues(@Param("userId") Long userId);
}
