package ma.entraide.gestionprojet.dto;

import ma.entraide.gestionprojet.entity.Tache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TacheDTO(
        Long id,
        String titre,
        String description,
        Long projetId,
        String projetNom,
        Long typeProjetId,
        Long tacheParentId,
        Boolean estModele,
        String priorite,
        String statut,
        LocalDate dateDebut,
        LocalDate dateEcheance,
        LocalDate dateFinReelle,
        Integer pourcentage,
        boolean enRetard,
        long joursRetard,
        // Recurrence fields
        Boolean estRecurrente,
        String periodicite,
        String regleRecurrence,
        Double dureeEstimeeHeures,
        LocalDate dateRealisee,
        LocalDate prochaineOccurrence,
        Integer occurrenceNumero,
        Long tacheRecurrenteParentId,
        // Metadata
        LocalDateTime dateCreation,
        List<AssigneeDTO> assignees,
        List<TacheDTO> sousTaches
) {
    public record AssigneeDTO(
            Long id,
            Long userId,
            String nomComplet,
            String email,
            String roleTache
    ) {}

    public static TacheDTO fromEntity(Tache tache) {
        List<AssigneeDTO> assignees = tache.getAssignees().stream()
                .map(ta -> new AssigneeDTO(
                        ta.getId(),
                        ta.getUser().getId(),
                        ta.getUser().getNomComplet(),
                        ta.getUser().getEmail(),
                        ta.getRoleTache().name()
                ))
                .toList();

        List<TacheDTO> sousTaches = tache.getSousTaches().stream()
                .map(TacheDTO::fromEntity)
                .toList();

        return new TacheDTO(
                tache.getId(),
                tache.getTitre(),
                tache.getDescription(),
                tache.getProjet().getId(),
                tache.getProjet().getNom(),
                tache.getTypeProjet() != null ? tache.getTypeProjet().getId() : null,
                tache.getTacheParent() != null ? tache.getTacheParent().getId() : null,
                tache.getEstModele(),
                tache.getPriorite().name(),
                tache.getStatut().name(),
                tache.getDateDebut(),
                tache.getDateEcheance(),
                tache.getDateFinReelle(),
                tache.getPourcentage(),
                tache.isEnRetard(),
                tache.getJoursRetard(),
                // Recurrence
                tache.getEstRecurrente(),
                tache.getPeriodicite() != null ? tache.getPeriodicite().name() : null,
                tache.getRegleRecurrence(),
                tache.getDureeEstimeeHeures(),
                tache.getDateRealisee(),
                tache.getProchaineOccurrence(),
                tache.getOccurrenceNumero(),
                tache.getTacheRecurrenteParent() != null ? tache.getTacheRecurrenteParent().getId() : null,
                // Metadata
                tache.getDateCreation(),
                assignees,
                sousTaches
        );
    }
}

