package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.ProjetDTO;
import ma.entraide.gestionprojet.dto.request.AddMembreRequest;
import ma.entraide.gestionprojet.dto.request.CreateProjetRequest;
import ma.entraide.gestionprojet.dto.request.UpdateProjetRequest;
import ma.entraide.gestionprojet.entity.*;
import ma.entraide.gestionprojet.entity.enums.Priorite;
import ma.entraide.gestionprojet.entity.enums.RoleProjet;
import ma.entraide.gestionprojet.entity.enums.StatutProjet;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import ma.entraide.gestionprojet.exception.BadRequestException;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final ProjetMembreRepository projetMembreRepository;
    private final TypeProjetRepository typeProjetRepository;
    private final TacheModeleRepository tacheModeleRepository;
    private final TacheRepository tacheRepository;
    private final EquipeService equipeService;
    private final UserService userService;

    public ProjetService(ProjetRepository projetRepository,
                         ProjetMembreRepository projetMembreRepository,
                         TypeProjetRepository typeProjetRepository,
                         TacheModeleRepository tacheModeleRepository,
                         TacheRepository tacheRepository,
                         EquipeService equipeService,
                         UserService userService) {
        this.projetRepository = projetRepository;
        this.projetMembreRepository = projetMembreRepository;
        this.typeProjetRepository = typeProjetRepository;
        this.tacheModeleRepository = tacheModeleRepository;
        this.tacheRepository = tacheRepository;
        this.equipeService = equipeService;
        this.userService = userService;
    }

    public List<ProjetDTO> getAllProjets() {
        return projetRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ProjetDTO> getProjetsByEquipe(Long equipeId) {
        return projetRepository.findByEquipeId(equipeId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ProjetDTO> getProjetsByUser(Long userId) {
        return projetRepository.findByMembreUserId(userId).stream()
                .map(this::toDTO)
                .toList();
    }

    public ProjetDTO getProjetById(Long id) {
        return toDTO(findProjetOrThrow(id));
    }

    public ProjetDTO createProjet(CreateProjetRequest request, Long createdById) {
        Equipe equipe = equipeService.findEquipeOrThrow(request.equipeId());
        User creator = userService.findUserOrThrow(createdById);
        TypeProjet typeProjet = typeProjetRepository.findById(request.typeProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Type de projet non trouve"));

        Projet projet = new Projet();
        projet.setNom(request.nom());
        projet.setDescription(request.description());
        projet.setEquipe(equipe);
        projet.setTypeProjet(typeProjet);
        projet.setPriorite(Priorite.valueOf(request.priorite()));
        projet.setStatut(StatutProjet.A_FAIRE);
        projet.setDateDebut(request.dateDebut());
        projet.setDateFinPrevue(request.dateFinPrevue());
        projet.setCreatedBy(creator);

        projet = projetRepository.save(projet);

        ProjetMembre pm = new ProjetMembre();
        pm.setProjet(projet);
        pm.setUser(creator);
        pm.setRoleProjet(RoleProjet.RESPONSABLE);
        projetMembreRepository.save(pm);

        if (!"AUTRE".equals(typeProjet.getCode())) {
            genererTachesParDefaut(projet, typeProjet, creator);
        }

        return toDTO(projetRepository.findById(projet.getId()).orElseThrow());
    }

    public ProjetDTO updateProjet(Long id, UpdateProjetRequest request) {
        Projet projet = findProjetOrThrow(id);

        if (request.nom() != null) projet.setNom(request.nom());
        if (request.description() != null) projet.setDescription(request.description());
        if (request.priorite() != null) projet.setPriorite(Priorite.valueOf(request.priorite()));
        if (request.statut() != null) {
            StatutProjet newStatut = StatutProjet.valueOf(request.statut());
            if (newStatut == StatutProjet.ANNULE) {
                projet.getTaches().stream()
                        .filter(t -> t.getStatut() != StatutTache.TERMINEE)
                        .forEach(t -> t.setStatut(StatutTache.BLOQUEE));
            }
            projet.setStatut(newStatut);
        }
        if (request.dateDebut() != null) projet.setDateDebut(request.dateDebut());
        if (request.dateFinPrevue() != null) projet.setDateFinPrevue(request.dateFinPrevue());
        if (request.dateFinReelle() != null) projet.setDateFinReelle(request.dateFinReelle());

        return toDTO(projetRepository.save(projet));
    }

    public void archiveProjet(Long id) {
        Projet projet = findProjetOrThrow(id);
        projet.setStatut(StatutProjet.ANNULE);
        projetRepository.save(projet);
    }

    public void deleteProjet(Long id) {
        Projet projet = findProjetOrThrow(id);
        // Delete all related data (members, tasks, etc.) via cascade or manually
        projetMembreRepository.deleteByProjetId(id);
        tacheRepository.deleteByProjetId(id);
        projetRepository.delete(projet);
    }

    public void addMembre(Long projetId, AddMembreRequest request) {
        Projet projet = findProjetOrThrow(projetId);
        User user = userService.findUserOrThrow(request.userId());

        if (projetMembreRepository.existsByProjetIdAndUserId(projetId, request.userId())) {
            throw new BadRequestException("Cet utilisateur fait deja partie du projet");
        }

        ProjetMembre pm = new ProjetMembre();
        pm.setProjet(projet);
        pm.setUser(user);
        pm.setRoleProjet(RoleProjet.valueOf(request.role()));
        projetMembreRepository.save(pm);
    }

    public void removeMembre(Long projetId, Long userId) {
        if (!projetMembreRepository.existsByProjetIdAndUserId(projetId, userId)) {
            throw new ResourceNotFoundException("Membre non trouve dans le projet");
        }
        projetMembreRepository.deleteByProjetIdAndUserId(projetId, userId);
    }

    public boolean isResponsable(Long projetId, Long userId) {
        return projetMembreRepository.existsByProjetIdAndUserIdAndRoleProjet(
                projetId, userId, RoleProjet.RESPONSABLE);
    }

    private void genererTachesParDefaut(Projet projet, TypeProjet typeProjet, User creator) {
        List<TacheModele> modeles = tacheModeleRepository.findByTypeProjetIdOrderByOrdreAsc(typeProjet.getId());

        for (TacheModele modele : modeles) {
            LocalDate dateEcheance = null;
            LocalDate dateDebutTache = null;

            if (projet.getDateDebut() != null && modele.getDelaiJours() != null) {
                dateEcheance = projet.getDateDebut().plusDays(modele.getDelaiJours());
                dateDebutTache = projet.getDateDebut();
            }

            Tache tache = new Tache();
            tache.setTitre(modele.getTitre());
            tache.setDescription(modele.getDescription());
            tache.setProjet(projet);
            tache.setTypeProjet(typeProjet);
            tache.setPriorite(modele.getPriorite());
            tache.setStatut(StatutTache.A_FAIRE);
            tache.setDateDebut(dateDebutTache);
            tache.setDateEcheance(dateEcheance != null ? dateEcheance : projet.getDateFinPrevue());
            tache.setEstModele(true);
            tache.setCreatedBy(creator);

            tacheRepository.save(tache);
        }
    }

    private ProjetDTO toDTO(Projet projet) {
        long tachesTerminees = projet.getTaches().stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINEE)
                .count();
        long tachesEnRetard = projet.getTaches().stream()
                .filter(Tache::isEnRetard)
                .count();
        return ProjetDTO.fromEntity(projet, tachesTerminees, tachesEnRetard);
    }

    public Projet findProjetOrThrow(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouve avec l'id : " + id));
    }
}


