package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.TacheDTO;
import ma.entraide.gestionprojet.dto.request.AddMembreRequest;
import ma.entraide.gestionprojet.dto.request.CreateTacheRequest;
import ma.entraide.gestionprojet.dto.request.UpdateTacheRequest;
import ma.entraide.gestionprojet.entity.Projet;
import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.TacheAssignee;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.entity.enums.Periodicite;
import ma.entraide.gestionprojet.entity.enums.Priorite;
import ma.entraide.gestionprojet.entity.enums.RoleTache;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import ma.entraide.gestionprojet.exception.BadRequestException;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.repository.TacheAssigneeRepository;
import ma.entraide.gestionprojet.repository.TacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TacheService {

    private final TacheRepository tacheRepository;
    private final TacheAssigneeRepository tacheAssigneeRepository;
    private final ProjetService projetService;
    private final UserService userService;

    public TacheService(TacheRepository tacheRepository,
                        TacheAssigneeRepository tacheAssigneeRepository,
                        ProjetService projetService,
                        UserService userService) {
        this.tacheRepository = tacheRepository;
        this.tacheAssigneeRepository = tacheAssigneeRepository;
        this.projetService = projetService;
        this.userService = userService;
    }

    public List<TacheDTO> getTachesByProjet(Long projetId) {
        return tacheRepository.findByProjetIdAndTacheParentIsNull(projetId).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public List<TacheDTO> getTachesByUser(Long userId) {
        return tacheRepository.findByAssigneeUserId(userId).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public List<TacheDTO> getActiveTachesByUser(Long userId) {
        return tacheRepository.findActiveTachesByUserId(userId).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public TacheDTO getTacheById(Long id) {
        return TacheDTO.fromEntity(findTacheOrThrow(id));
    }

    public TacheDTO createTache(CreateTacheRequest request, Long createdById) {
        Projet projet = projetService.findProjetOrThrow(request.projetId());
        User creator = userService.findUserOrThrow(createdById);

        Tache tache = new Tache();
        tache.setTitre(request.titre());
        tache.setDescription(request.description());
        tache.setProjet(projet);
        tache.setTypeProjet(projet.getTypeProjet());
        tache.setPriorite(Priorite.valueOf(request.priorite()));
        tache.setDateDebut(request.dateDebut());
        tache.setDateEcheance(request.dateEcheance());
        tache.setCreatedBy(creator);

        if (request.tacheParentId() != null) {
            Tache parent = findTacheOrThrow(request.tacheParentId());
            tache.setTacheParent(parent);
        }

        if (projet.getDateFinPrevue() != null && request.dateEcheance().isAfter(projet.getDateFinPrevue())) {
            throw new BadRequestException(
                    "La date d'echeance de la tache ne peut pas depasser la deadline du projet (" +
                            projet.getDateFinPrevue() + ")"
            );
        }

        // Handle recurrence
        boolean isRecurrente = request.estRecurrente() != null && request.estRecurrente();
        tache.setEstRecurrente(isRecurrente);
        if (isRecurrente) {
            if (request.periodicite() == null) {
                throw new BadRequestException("La periodicite est obligatoire pour une tache recurrente");
            }
            Periodicite periodicite = Periodicite.valueOf(request.periodicite());
            tache.setPeriodicite(periodicite);
            tache.setRegleRecurrence(request.regleRecurrence());
            tache.setStatut(StatutTache.PLANIFIEE);
            tache.setProchaineOccurrence(calculerProchaineOccurrence(request.dateEcheance(), periodicite));
            tache.setOccurrenceNumero(1);
        } else {
            tache.setStatut(StatutTache.A_FAIRE);
        }

        if (request.dureeEstimeeHeures() != null) {
            tache.setDureeEstimeeHeures(request.dureeEstimeeHeures());
        }

        return TacheDTO.fromEntity(tacheRepository.save(tache));
    }

    public TacheDTO updateTache(Long id, UpdateTacheRequest request) {
        Tache tache = findTacheOrThrow(id);

        if (request.titre() != null) tache.setTitre(request.titre());
        if (request.description() != null) tache.setDescription(request.description());
        if (request.priorite() != null) tache.setPriorite(Priorite.valueOf(request.priorite()));
        if (request.statut() != null) {
            StatutTache newStatut = StatutTache.valueOf(request.statut());
            handleStatutChange(tache, newStatut);
        }
        if (request.dateDebut() != null) tache.setDateDebut(request.dateDebut());
        if (request.dateEcheance() != null) tache.setDateEcheance(request.dateEcheance());
        if (request.pourcentage() != null) {
            if (request.pourcentage() < 0 || request.pourcentage() > 100) {
                throw new BadRequestException("Le pourcentage doit etre entre 0 et 100");
            }
            tache.setPourcentage(request.pourcentage());
        }

        // Update recurrence fields
        if (request.estRecurrente() != null) tache.setEstRecurrente(request.estRecurrente());
        if (request.periodicite() != null) tache.setPeriodicite(Periodicite.valueOf(request.periodicite()));
        if (request.regleRecurrence() != null) tache.setRegleRecurrence(request.regleRecurrence());
        if (request.dureeEstimeeHeures() != null) tache.setDureeEstimeeHeures(request.dureeEstimeeHeures());

        // Recalculate prochaine occurrence if periodicite or echeance changed
        if (tache.getEstRecurrente() && (request.periodicite() != null || request.dateEcheance() != null)) {
            LocalDate baseDate = tache.getDateEcheance();
            tache.setProchaineOccurrence(calculerProchaineOccurrence(baseDate, tache.getPeriodicite()));
        }

        return TacheDTO.fromEntity(tacheRepository.save(tache));
    }

    public void deleteTache(Long id) {
        Tache tache = findTacheOrThrow(id);
        tacheRepository.delete(tache);
    }

    public TacheDTO changeStatut(Long id, String statut) {
        Tache tache = findTacheOrThrow(id);
        StatutTache newStatut = StatutTache.valueOf(statut);
        handleStatutChange(tache, newStatut);
        return TacheDTO.fromEntity(tacheRepository.save(tache));
    }

    public TacheDTO updateAvancement(Long id, int pourcentage) {
        if (pourcentage < 0 || pourcentage > 100) {
            throw new BadRequestException("Le pourcentage doit etre entre 0 et 100");
        }
        Tache tache = findTacheOrThrow(id);
        tache.setPourcentage(pourcentage);
        if (pourcentage == 100) {
            handleStatutChange(tache, StatutTache.TERMINEE);
        }
        return TacheDTO.fromEntity(tacheRepository.save(tache));
    }

    public void assignUser(Long tacheId, AddMembreRequest request) {
        Tache tache = findTacheOrThrow(tacheId);
        User user = userService.findUserOrThrow(request.userId());

        if (tacheAssigneeRepository.existsByTacheIdAndUserId(tacheId, request.userId())) {
            throw new BadRequestException("Cet utilisateur est deja assigne a cette tache");
        }

        TacheAssignee assignee = new TacheAssignee();
        assignee.setTache(tache);
        assignee.setUser(user);
        assignee.setRoleTache(RoleTache.valueOf(request.role()));
        tacheAssigneeRepository.save(assignee);
    }

    public void unassignUser(Long tacheId, Long userId) {
        if (!tacheAssigneeRepository.existsByTacheIdAndUserId(tacheId, userId)) {
            throw new ResourceNotFoundException("Assignation non trouvee");
        }
        tacheAssigneeRepository.deleteByTacheIdAndUserId(tacheId, userId);
    }

    // --- Recurrence helpers ---

    /**
     * When a recurring task status changes to TERMINEE,
     * mark dateRealisee and keep prochaineOccurrence for the scheduler to generate the next one.
     */
    private void handleStatutChange(Tache tache, StatutTache newStatut) {
        tache.setStatut(newStatut);
        if (newStatut == StatutTache.TERMINEE) {
            tache.setDateFinReelle(LocalDate.now());
            tache.setDateRealisee(LocalDate.now());
            tache.setPourcentage(100);
            // If recurrente, prochaineOccurrence is already set -- the scheduler will pick it up
        }
    }

    /**
     * Calculate the next occurrence date based on the current echeance and periodicite.
     * For CONTINU and A_LA_DEMANDE: no auto-scheduled next occurrence.
     */
    public static LocalDate calculerProchaineOccurrence(LocalDate fromDate, Periodicite periodicite) {
        if (fromDate == null || periodicite == null) return null;
        return switch (periodicite) {
            case HEBDOMADAIRE -> fromDate.plusWeeks(1);
            case MENSUEL -> fromDate.plusMonths(1);
            case TRIMESTRIEL -> fromDate.plusMonths(3);
            case SEMESTRIEL -> fromDate.plusMonths(6);
            case ANNUEL -> fromDate.plusYears(1);
            case CONTINU, A_LA_DEMANDE -> null; // No auto-generation; manual trigger
        };
    }

    // --- Taches recurrentes queries ---

    public List<TacheDTO> getTachesRecurrentesByProjet(Long projetId) {
        return tacheRepository.findTachesRecurrentesByProjet(projetId).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public List<TacheDTO> getAllTachesRecurrentes() {
        return tacheRepository.findAllTachesRecurrentes().stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public List<TacheDTO> getOccurrenceHistory(Long tacheId) {
        return tacheRepository.findOccurrencesByParent(tacheId).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public Tache findTacheOrThrow(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tache non trouvee avec l'id : " + id));
    }
}

