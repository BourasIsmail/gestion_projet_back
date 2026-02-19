package ma.entraide.gestionprojet.rapport.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour le rapport d'activite d'une equipe.
 */
public record RapportEquipeData(
        String nomEquipe,
        String description,
        String chefEquipe,
        int nombreMembres,

        List<MembreInfo> membres,
        List<ProjetResume> projets,

        // KPIs
        long projetsActifs,
        long projetsTermines,
        long totalTaches,
        long tachesTerminees,
        long tachesEnRetard,
        double tauxCompletion,
        double delaiMoyenRetard
) {
    public record MembreInfo(String nom, String role) {}

    public record ProjetResume(
            String nom,
            String type,
            String statut,
            String priorite,
            int progression,
            LocalDate deadline,
            long tachesEnRetard
    ) {}
}
