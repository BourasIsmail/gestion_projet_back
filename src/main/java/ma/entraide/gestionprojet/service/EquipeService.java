package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.EquipeDTO;
import ma.entraide.gestionprojet.dto.request.AddMembreRequest;
import ma.entraide.gestionprojet.dto.request.CreateEquipeRequest;
import ma.entraide.gestionprojet.entity.Equipe;
import ma.entraide.gestionprojet.entity.EquipeMembre;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.entity.enums.RoleEquipe;
import ma.entraide.gestionprojet.exception.BadRequestException;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.repository.EquipeMembreRepository;
import ma.entraide.gestionprojet.repository.EquipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final EquipeMembreRepository equipeMembreRepository;
    private final UserService userService;

    public EquipeService(EquipeRepository equipeRepository,
                         EquipeMembreRepository equipeMembreRepository,
                         UserService userService) {
        this.equipeRepository = equipeRepository;
        this.equipeMembreRepository = equipeMembreRepository;
        this.userService = userService;
    }

    public List<EquipeDTO> getAllEquipes() {
        return equipeRepository.findByActifTrue().stream()
                .map(EquipeDTO::fromEntity)
                .toList();
    }

    public List<EquipeDTO> getEquipesByUserId(Long userId) {
        return equipeRepository.findByMembreUserId(userId).stream()
                .map(EquipeDTO::fromEntity)
                .toList();
    }

    public EquipeDTO getEquipeById(Long id) {
        return EquipeDTO.fromEntity(findEquipeOrThrow(id));
    }

    public EquipeDTO createEquipe(CreateEquipeRequest request, Long createdById) {
        if (equipeRepository.existsByNom(request.nom())) {
            throw new BadRequestException("Une equipe avec ce nom existe deja");
        }

        User creator = userService.findUserOrThrow(createdById);

        Equipe equipe = new Equipe();
        equipe.setNom(request.nom());
        equipe.setDescription(request.description());
        equipe.setCreatedBy(creator);
        equipe.setActif(true);

        equipe = equipeRepository.save(equipe);

        EquipeMembre membre = new EquipeMembre();
        membre.setEquipe(equipe);
        membre.setUser(creator);
        membre.setRoleEquipe(RoleEquipe.CHEF_EQUIPE);
        equipeMembreRepository.save(membre);

        return EquipeDTO.fromEntity(equipeRepository.findById(equipe.getId()).orElseThrow());
    }

    public EquipeDTO updateEquipe(Long id, String nom, String description) {
        Equipe equipe = findEquipeOrThrow(id);
        if (nom != null) equipe.setNom(nom);
        if (description != null) equipe.setDescription(description);
        return EquipeDTO.fromEntity(equipeRepository.save(equipe));
    }

    public void archiveEquipe(Long id) {
        Equipe equipe = findEquipeOrThrow(id);
        equipe.setActif(false);
        equipeRepository.save(equipe);
    }

    public void addMembre(Long equipeId, AddMembreRequest request) {
        Equipe equipe = findEquipeOrThrow(equipeId);
        User user = userService.findUserOrThrow(request.userId());

        if (equipeMembreRepository.existsByEquipeIdAndUserId(equipeId, request.userId())) {
            throw new BadRequestException("Cet utilisateur fait deja partie de l'equipe");
        }

        EquipeMembre membre = new EquipeMembre();
        membre.setEquipe(equipe);
        membre.setUser(user);
        membre.setRoleEquipe(RoleEquipe.valueOf(request.role()));
        equipeMembreRepository.save(membre);
    }

    public void removeMembre(Long equipeId, Long userId) {
        if (!equipeMembreRepository.existsByEquipeIdAndUserId(equipeId, userId)) {
            throw new ResourceNotFoundException("Membre non trouve dans l'equipe");
        }
        equipeMembreRepository.deleteByEquipeIdAndUserId(equipeId, userId);
    }

    public void changeRoleMembre(Long equipeId, Long userId, String role) {
        EquipeMembre membre = equipeMembreRepository.findByEquipeIdAndUserId(equipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouve dans l'equipe"));
        membre.setRoleEquipe(RoleEquipe.valueOf(role));
        equipeMembreRepository.save(membre);
    }

    public boolean isChefEquipe(Long equipeId, Long userId) {
        return equipeMembreRepository.existsByEquipeIdAndUserIdAndRoleEquipe(
                equipeId, userId, RoleEquipe.CHEF_EQUIPE);
    }

    public Equipe findEquipeOrThrow(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe non trouvee avec l'id : " + id));
    }
}
