package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.auth.AuthResponse;
import ma.entraide.gestionprojet.dto.auth.LoginRequest;
import ma.entraide.gestionprojet.dto.auth.RefreshTokenRequest;
import ma.entraide.gestionprojet.dto.auth.RegisterRequest;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.entity.enums.RoleGlobal;
import ma.entraide.gestionprojet.exception.BadRequestException;
import ma.entraide.gestionprojet.repository.UserRepository;
import ma.entraide.gestionprojet.security.JwtService;
import ma.entraide.gestionprojet.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Utilisateur non trouve"));

        UserDetailsImpl userDetails = UserDetailsImpl.fromUser(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getNom(),
                        user.getPrenom(),
                        user.getEmail(),
                        user.getRoleGlobal().name(),
                        user.getAvatarUrl()
                )
        );
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Cet email est deja utilise");
        }

        User user = new User();
        user.setNom(request.nom());
        user.setPrenom(request.prenom());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoleGlobal(RoleGlobal.valueOf(request.roleGlobal()));
        user.setActif(true);

        userRepository.save(user);

        UserDetailsImpl userDetails = UserDetailsImpl.fromUser(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getNom(),
                        user.getPrenom(),
                        user.getEmail(),
                        user.getRoleGlobal().name(),
                        user.getAvatarUrl()
                )
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String userEmail = jwtService.extractUsername(request.refreshToken());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("Token invalide"));

        UserDetailsImpl userDetails = UserDetailsImpl.fromUser(user);

        if (!jwtService.isTokenValid(request.refreshToken(), userDetails)) {
            throw new BadRequestException("Refresh token invalide ou expire");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);

        return new AuthResponse(
                accessToken,
                request.refreshToken(),
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getNom(),
                        user.getPrenom(),
                        user.getEmail(),
                        user.getRoleGlobal().name(),
                        user.getAvatarUrl()
                )
        );
    }
}

