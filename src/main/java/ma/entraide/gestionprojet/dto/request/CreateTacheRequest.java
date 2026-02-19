package ma.entraide.gestionprojet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTacheRequest(
        @NotBlank @Size(max = 300) String titre,
        String description,
        @NotNull Long projetId,
        Long tacheParentId,
        @NotBlank String priorite,
        LocalDate dateDebut,
        @NotNull LocalDate dateEcheance,
        // Recurrence fields
        Boolean estRecurrente,
        String periodicite,
        @Size(max = 500) String regleRecurrence,
        Double dureeEstimeeHeures
) {}
