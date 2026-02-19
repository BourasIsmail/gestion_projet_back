package ma.entraide.gestionprojet.dto;

import ma.entraide.gestionprojet.entity.Equipe;

import java.time.LocalDateTime;
import java.util.List;

public record EquipeDTO(
        Long id,
        String nom,
        String description,
        UserDTO createdBy,
        Boolean actif,
        LocalDateTime dateCreation,
        int nombreMembres,
        List<MembreDTO> membres
) {
    public record MembreDTO(
            Long id,
            Long userId,
            String nom,
            String prenom,
            String email,
            String roleEquipe,
            LocalDateTime dateAjout
    ) {}

    public static EquipeDTO fromEntity(Equipe equipe) {
        List<MembreDTO> membres = equipe.getMembres().stream()
                .map(em -> new MembreDTO(
                        em.getId(),
                        em.getUser().getId(),
                        em.getUser().getNom(),
                        em.getUser().getPrenom(),
                        em.getUser().getEmail(),
                        em.getRoleEquipe().name(),
                        em.getDateAjout()
                ))
                .toList();

        return new EquipeDTO(
                equipe.getId(),
                equipe.getNom(),
                equipe.getDescription(),
                UserDTO.fromEntity(equipe.getCreatedBy()),
                equipe.getActif(),
                equipe.getDateCreation(),
                membres.size(),
                membres
        );
    }
}

