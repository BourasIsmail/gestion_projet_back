package ma.entraide.gestionprojet.rapport.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO contenant toutes les donnees necessaires a la generation
 * du rapport PDF d'un projet.
 */
public record RapportProjetData(
        // Infos projet
        String nomProjet,
        String description,
        String typeProjet,
        String equipe,
        String priorite,
        String statut,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        LocalDate dateFinReelle,
        int pourcentageProgression,
        String creePar,

        // Membres
        List<MembreInfo> membres,

        // Taches
        List<TacheInfo> taches,

        // KPIs
        long totalTaches,
        long tachesTerminees,
        long tachesEnCours,
        long tachesEnRetard,
        long tachesAFaire
) {
    public record MembreInfo(String nom, String role) {}

    public record TacheInfo(
            String titre,
            String priorite,
            String statut,
            String assignes,
            LocalDate dateEcheance,
            int pourcentage,
            long joursRetard,
            boolean enRetard,
            boolean procheDeadline
    ) {}
}
