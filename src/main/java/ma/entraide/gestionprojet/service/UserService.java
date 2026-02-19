package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.UserDTO;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.entity.enums.RoleGlobal;
import ma.entraide.gestionprojet.exception.BadRequestException;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    public UserDTO getUserById(Long id) {
        return UserDTO.fromEntity(findUserOrThrow(id));
    }

    public UserDTO updateUser(Long id, String nom, String prenom, String email, String avatarUrl) {
        User user = findUserOrThrow(id);
        if (nom != null) user.setNom(nom);
        if (prenom != null) user.setPrenom(prenom);
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new BadRequestException("Cet email est deja utilise");
            }
            user.setEmail(email);
        }
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        return UserDTO.fromEntity(userRepository.save(user));
    }

    public void changeRole(Long id, String role) {
        User user = findUserOrThrow(id);
        user.setRoleGlobal(RoleGlobal.valueOf(role));
        userRepository.save(user);
    }

    public void deactivateUser(Long id) {
        User user = findUserOrThrow(id);
        user.setActif(false);
        userRepository.save(user);
    }

    public User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve avec l'id : " + id));
    }
}

