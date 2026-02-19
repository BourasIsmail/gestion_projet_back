package ma.entraide.gestionprojet.dto;

import ma.entraide.gestionprojet.entity.Notification;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        String titre,
        String message,
        String type,
        String referenceType,
        Long referenceId,
        Boolean lue,
        LocalDateTime dateCreation
) {
    public static NotificationDTO fromEntity(Notification notif) {
        return new NotificationDTO(
                notif.getId(),
                notif.getTitre(),
                notif.getMessage(),
                notif.getType().name(),
                notif.getReferenceType() != null ? notif.getReferenceType().name() : null,
                notif.getReferenceId(),
                notif.getLue(),
                notif.getDateCreation()
        );
    }
}

