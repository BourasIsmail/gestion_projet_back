package ma.entraide.gestionprojet.dto;


import ma.entraide.gestionprojet.entity.Projet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjetDTO(
        Long id,
        String nom,
        String description,
        Long equipeId,
        String equipeNom,
        Long typeProjetId,
        String typeProjetLibelle,
        String priorite,
        String statut,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        LocalDate dateFinReelle,
        int pourcentageProgression,
        LocalDateTime dateCreation,
        List<MembreDTO> membres,
        long nombreTaches,
        long tachesTerminees,
        long tachesEnRetard
) {
    public record MembreDTO(
            Long id,
            Long userId,
            String nomComplet,
            String email,
            String roleProjet
    ) {}

    public static ProjetDTO fromEntity(Projet projet, long tachesTerminees, long tachesEnRetard) {
        List<MembreDTO> membres = projet.getMembres().stream()
                .map(pm -> new MembreDTO(
                        pm.getId(),
                        pm.getUser().getId(),
                        pm.getUser().getNomComplet(),
                        pm.getUser().getEmail(),
                        pm.getRoleProjet().name()
                ))
                .toList();

        return new ProjetDTO(
                projet.getId(),
                projet.getNom(),
                projet.getDescription(),
                projet.getEquipe().getId(),
                projet.getEquipe().getNom(),
                projet.getTypeProjet().getId(),
                projet.getTypeProjet().getLibelle(),
                projet.getPriorite().name(),
                projet.getStatut().name(),
                projet.getDateDebut(),
                projet.getDateFinPrevue(),
                projet.getDateFinReelle(),
                projet.getPourcentageProgression(),
                projet.getDateCreation(),
                membres,
                projet.getTaches().size(),
                tachesTerminees,
                tachesEnRetard
        );
    }
}

