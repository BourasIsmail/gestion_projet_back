package ma.entraide.gestionprojet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjetRequest(
        @NotBlank @Size(max = 200) String nom,
        String description,
        @NotNull Long equipeId,
        @NotNull Long typeProjetId,
        @NotBlank String priorite,
        LocalDate dateDebut,
        LocalDate dateFinPrevue
) {}
