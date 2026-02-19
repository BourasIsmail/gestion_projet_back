package ma.entraide.gestionprojet.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProjetRequest(
        @Size(max = 200) String nom,
        String description,
        String priorite,
        String statut,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        LocalDate dateFinReelle
) {}
