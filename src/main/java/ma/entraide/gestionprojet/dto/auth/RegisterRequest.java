package ma.entraide.gestionprojet.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String roleGlobal
) {}

