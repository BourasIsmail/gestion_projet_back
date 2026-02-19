package ma.entraide.gestionprojet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEquipeRequest(
        @NotBlank @Size(max = 200) String nom,
        String description
) {}
