package ma.entraide.gestionprojet.dto;

import java.util.List;

public record DashboardDTO(
        StatsDTO stats,
        List<TacheDTO> mesTachesEnCours,
        List<TacheDTO> tachesEnRetard,
        List<TacheDTO> tachesProchesDeadline
) {
    public record StatsDTO(
            long totalProjets,
            long projetsActifs,
            long projetsTermines,
            long totalTaches,
            long tachesTerminees,
            long tachesEnRetard,
            long totalEquipes,
            long totalUtilisateurs
    ) {}
}

