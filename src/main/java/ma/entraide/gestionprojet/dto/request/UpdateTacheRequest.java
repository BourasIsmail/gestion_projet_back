package ma.entraide.gestionprojet.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTacheRequest(
        @Size(max = 300) String titre,
        String description,
        String priorite,
        String statut,
        LocalDate dateDebut,
        LocalDate dateEcheance,
        Integer pourcentage,
        // Recurrence fields
        Boolean estRecurrente,
        String periodicite,
        @Size(max = 500) String regleRecurrence,
        Double dureeEstimeeHeures
) {}
