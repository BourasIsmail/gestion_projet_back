package ma.entraide.gestionprojet.rapport.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour le rapport des alertes et retards.
 */
public record RapportAlertesData(
        long totalRetards,
        double retardMoyenJours,
        double tauxRespectDelais,

        List<TacheAlerte> tachesEnRetard,
        List<TacheAlerte> tachesProchesDeadline
) {
    public record TacheAlerte(
            String projet,
            String equipe,
            String tache,
            String priorite,
            String assignes,
            LocalDate dateEcheance,
            long joursRetardOuRestants
    ) {}
}
