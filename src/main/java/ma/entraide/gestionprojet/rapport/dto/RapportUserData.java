package ma.entraide.gestionprojet.rapport.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour le rapport d'activite d'un utilisateur.
 */
public record RapportUserData(
        String nomComplet,
        String email,
        String roleGlobal,
        List<String> equipes,

        List<TacheUser> taches,

        // KPIs
        long tachesCompletees,
        long tachesEnCours,
        long tachesEnRetard,
        double tauxCompletion
) {
    public record TacheUser(
            String projet,
            String titre,
            String priorite,
            String statut,
            LocalDate dateEcheance,
            int pourcentage,
            boolean enRetard
    ) {}
}

