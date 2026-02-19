package ma.entraide.gestionprojet.dto;

import ma.entraide.gestionprojet.entity.User;

import java.time.LocalDateTime;

public record UserDTO(
        Long id,
        String nom,
        String prenom,
        String email,
        String roleGlobal,
        String avatarUrl,
        Boolean actif,
        LocalDateTime dateCreation
) {
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRoleGlobal().name(),
                user.getAvatarUrl(),
                user.getActif(),
                user.getDateCreation()
        );
    }
}
