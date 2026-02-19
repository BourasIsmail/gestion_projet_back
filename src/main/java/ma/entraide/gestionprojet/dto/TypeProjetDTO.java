package ma.entraide.gestionprojet.dto;

import ma.entraide.gestionprojet.entity.TypeProjet;

import java.util.List;

public record TypeProjetDTO(
        Long id,
        String code,
        String libelle,
        String description,
        Boolean actif,
        List<TacheModeleDTO> tachesModeles
) {
    public record TacheModeleDTO(
            Long id,
            String titre,
            String description,
            String priorite,
            Integer ordre,
            Integer delaiJours
    ) {}

    public static TypeProjetDTO fromEntity(TypeProjet tp) {
        List<TacheModeleDTO> modeles = tp.getTachesModeles().stream()
                .map(tm -> new TacheModeleDTO(
                        tm.getId(),
                        tm.getTitre(),
                        tm.getDescription(),
                        tm.getPriorite().name(),
                        tm.getOrdre(),
                        tm.getDelaiJours()
                ))
                .toList();

        return new TypeProjetDTO(
                tp.getId(),
                tp.getCode(),
                tp.getLibelle(),
                tp.getDescription(),
                tp.getActif(),
                modeles
        );
    }
}

