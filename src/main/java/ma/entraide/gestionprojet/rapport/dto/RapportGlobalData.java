package ma.entraide.gestionprojet.rapport.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO pour le rapport global de synthese (Admin).
 */
public record RapportGlobalData(
        // KPIs generaux
        long utilisateursActifs,
        long nombreEquipes,
        long nombreProjets,
        long nombreTaches,

        // Repartitions
        Map<String, Long> projetsParType,
        Map<String, Long> projetsParStatut,

        // Top 5
        List<ProjetRetard> top5ProjetsEnRetard,
        List<EquipePerf> top5EquipesPerformantes,

        // Alertes
        long alertesActives,
        long tachesEnRetardTotal
) {
    public record ProjetRetard(String nom, String equipe, long joursRetard) {}
    public record EquipePerf(String nom, double tauxCompletion, long projetsTermines) {}
}
