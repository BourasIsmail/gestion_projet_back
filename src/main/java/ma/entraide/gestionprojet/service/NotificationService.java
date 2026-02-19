package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.NotificationDTO;
import ma.entraide.gestionprojet.entity.Notification;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.entity.enums.ReferenceType;
import ma.entraide.gestionprojet.entity.enums.TypeNotification;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<NotificationDTO> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId, pageable)
                .map(NotificationDTO::fromEntity);
    }

    public long countNonLues(Long userId) {
        return notificationRepository.countByUserIdAndLueFalse(userId);
    }

    public void marquerCommeLue(Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvee"));
        notif.setLue(true);
        notificationRepository.save(notif);
    }

    public void marquerToutesCommeLues(Long userId) {
        notificationRepository.marquerToutesCommeLues(userId);
    }

    public Notification creerNotification(User destinataire, String titre, String message,
                                          TypeNotification type, ReferenceType refType, Long refId) {
        Notification notif = new Notification();
        notif.setUser(destinataire);
        notif.setTitre(titre);
        notif.setMessage(message);
        notif.setType(type);
        notif.setReferenceType(refType);
        notif.setReferenceId(refId);
        notif.setLue(false);
        return notificationRepository.save(notif);
    }
}

