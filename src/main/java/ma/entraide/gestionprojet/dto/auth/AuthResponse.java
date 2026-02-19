package ma.entraide.gestionprojet.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserInfo user
) {
    public record UserInfo(
            Long id,
            String nom,
            String prenom,
            String email,
            String roleGlobal,
            String avatarUrl
    ) {}
}