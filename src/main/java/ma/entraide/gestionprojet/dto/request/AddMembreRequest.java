package ma.entraide.gestionprojet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMembreRequest(
        @NotNull Long userId,
        @NotBlank String role
) {}
