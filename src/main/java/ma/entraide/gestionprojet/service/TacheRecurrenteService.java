package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.TacheAssignee;
import ma.entraide.gestionprojet.entity.enums.Periodicite;
import ma.entraide.gestionprojet.entity.enums.ReferenceType;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import ma.entraide.gestionprojet.entity.enums.TypeNotification;
import ma.entraide.gestionprojet.repository.TacheAssigneeRepository;
import ma.entraide.gestionprojet.repository.TacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de gestion automatique des taches recurrentes.
 *
 * Fonctionnement:
 * 1. Chaque jour a 07h00, verifie les taches recurrentes terminees dont la prochaineOccurrence est arrivee
 * 2. Genere automatiquement une nouvelle occurrence (clone de la tache originale)
 * 3. Active les taches PLANIFIEES dont la date de debut est arrivee
 * 4. Notifie les responsables assignes
 */
@Service
@Transactional
public class TacheRecurrenteService {

    private static final Logger log = LoggerFactory.getLogger(TacheRecurrenteService.class);

    private final TacheRepository tacheRepository;
    private final TacheAssigneeRepository tacheAssigneeRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public TacheRecurrenteService(TacheRepository tacheRepository,
                                  TacheAssigneeRepository tacheAssigneeRepository,
                                  NotificationService notificationService,
                                  EmailService emailService) {
        this.tacheRepository = tacheRepository;
        this.tacheAssigneeRepository = tacheAssigneeRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Job quotidien a 07h00: generer les nouvelles occurrences des taches recurrentes.
     */
    @Scheduled(cron = "${app.recurrence.cron-generate:0 0 7 * * *}")
    public void genererOccurrencesRecurrentes() {
        log.info("Demarrage de la generation des occurrences recurrentes...");
        LocalDate today = LocalDate.now();

        // 1. Taches recurrentes terminees dont la prochaine occurrence est aujourd'hui ou passee
        List<Tache> tachesARegenerer = tacheRepository.findTachesRecurrentesARegenerer(today);
        log.info("{} tache(s) recurrente(s) a regenerer", tachesARegenerer.size());

        for (Tache tacheTerminee : tachesARegenerer) {
            try {
                Tache nouvelleOccurrence = creerNouvelleOccurrence(tacheTerminee);
                notifierAssignes(nouvelleOccurrence, tacheTerminee);
                log.info("Nouvelle occurrence creee pour '{}' (occurrence #{}) - echeance: {}",
                        nouvelleOccurrence.getTitre(),
                        nouvelleOccurrence.getOccurrenceNumero(),
                        nouvelleOccurrence.getDateEcheance());
            } catch (Exception e) {
                log.error("Erreur lors de la generation d'occurrence pour la tache {}: {}",
                        tacheTerminee.getId(), e.getMessage(), e);
            }
        }

        // 2. Activer les taches planifiees dont la date de debut est arrivee
        List<Tache> tachesAActiver = tacheRepository.findTachesPlanifieesAActiver(today);
        log.info("{} tache(s) planifiee(s) a activer", tachesAActiver.size());

        for (Tache tache : tachesAActiver) {
            tache.setStatut(StatutTache.A_FAIRE);
            tacheRepository.save(tache);
            log.info("Tache '{}' activee (PLANIFIEE -> A_FAIRE)", tache.getTitre());
        }

        log.info("Generation des occurrences recurrentes terminee.");
    }

    /**
     * Cree une nouvelle occurrence en clonant la tache recurrente terminee.
     * La tache originale reste en TERMINEE avec sa dateRealisee.
     * La nouvelle occurrence herite des assignees, de la periodicite, et a de nouvelles dates.
     */
    private Tache creerNouvelleOccurrence(Tache source) {
        LocalDate nouvelleEcheance = source.getProchaineOccurrence();
        Periodicite periodicite = source.getPeriodicite();

        // Calculer la duree originale entre debut et echeance pour reproduire le meme intervalle
        long dureeJours = 0;
        if (source.getDateDebut() != null && source.getDateEcheance() != null) {
            dureeJours = source.getDateDebut().until(source.getDateEcheance()).getDays();
        }
        LocalDate nouveauDebut = (dureeJours > 0)
                ? nouvelleEcheance.minusDays(dureeJours)
                : nouvelleEcheance;

        // Determiner le parent de la chaine de recurrence
        Tache parentRecurrence = source.getTacheRecurrenteParent() != null
                ? source.getTacheRecurrenteParent()
                : source;

        int nouveauNumero = source.getOccurrenceNumero() != null
                ? source.getOccurrenceNumero() + 1
                : 2;

        // Create the new occurrence
        Tache occurrence = new Tache();
        occurrence.setTitre(source.getTitre());
        occurrence.setDescription(source.getDescription());
        occurrence.setProjet(source.getProjet());
        occurrence.setTypeProjet(source.getTypeProjet());
        occurrence.setTacheParent(source.getTacheParent());
        occurrence.setEstModele(false);
        occurrence.setPriorite(source.getPriorite());
        occurrence.setStatut(StatutTache.PLANIFIEE);
        occurrence.setDateDebut(nouveauDebut);
        occurrence.setDateEcheance(nouvelleEcheance);
        occurrence.setPourcentage(0);
        occurrence.setCreatedBy(source.getCreatedBy());

        // Recurrence fields
        occurrence.setEstRecurrente(true);
        occurrence.setPeriodicite(periodicite);
        occurrence.setRegleRecurrence(source.getRegleRecurrence());
        occurrence.setDureeEstimeeHeures(source.getDureeEstimeeHeures());
        occurrence.setOccurrenceNumero(nouveauNumero);
        occurrence.setTacheRecurrenteParent(parentRecurrence);
        occurrence.setProchaineOccurrence(
                TacheService.calculerProchaineOccurrence(nouvelleEcheance, periodicite)
        );

        // Alert flags reset for the new occurrence
        occurrence.setAlerteEnvoyeeApproche(false);
        occurrence.setAlerteEnvoyeeRetard(false);

        Tache saved = tacheRepository.save(occurrence);

        // Clone assignees from the source to the new occurrence
        List<TacheAssignee> sourceAssignees = tacheAssigneeRepository.findByTacheId(source.getId());
        for (TacheAssignee sa : sourceAssignees) {
            TacheAssignee newAssignee = new TacheAssignee();
            newAssignee.setTache(saved);
            newAssignee.setUser(sa.getUser());
            newAssignee.setRoleTache(sa.getRoleTache());
            tacheAssigneeRepository.save(newAssignee);
        }

        // Clear prochaineOccurrence on the completed source (it's been handled)
        source.setProchaineOccurrence(null);
        tacheRepository.save(source);

        return saved;
    }

    /**
     * Notifie les assignes de la tache source qu'une nouvelle occurrence a ete creee.
     */
    private void notifierAssignes(Tache nouvelleOccurrence, Tache ancienneTache) {
        String periodiciteLabel = ancienneTache.getPeriodicite() != null
                ? ancienneTache.getPeriodicite().name().toLowerCase().replace('_', ' ')
                : "recurrente";

        String titre = "Nouvelle occurrence (" + periodiciteLabel + ") : " + nouvelleOccurrence.getTitre();
        String message = String.format(
                "La tache recurrente '%s' du projet '%s' a genere une nouvelle occurrence (#%d). " +
                        "Echeance : %s.",
                nouvelleOccurrence.getTitre(),
                nouvelleOccurrence.getProjet().getNom(),
                nouvelleOccurrence.getOccurrenceNumero(),
                nouvelleOccurrence.getDateEcheance()
        );

        List<TacheAssignee> assignees = tacheAssigneeRepository.findByTacheId(nouvelleOccurrence.getId());
        for (TacheAssignee assignee : assignees) {
            notificationService.creerNotification(
                    assignee.getUser(), titre, message,
                    TypeNotification.INFO,
                    ReferenceType.TACHE, nouvelleOccurrence.getId()
            );
            emailService.envoyerNotificationNouvelleOccurrence(
                    assignee.getUser(), nouvelleOccurrence
            );
        }
    }

    /**
     * Declenchement manuel d'une nouvelle occurrence (pour les taches A_LA_DEMANDE ou CONTINU).
     */
    public Tache genererOccurrenceManuellement(Long tacheId) {
        Tache source = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tache non trouvee : " + tacheId));

        if (!source.getEstRecurrente()) {
            throw new RuntimeException("La tache n'est pas recurrente");
        }

        Periodicite p = source.getPeriodicite();
        if (p != Periodicite.CONTINU && p != Periodicite.A_LA_DEMANDE) {
            throw new RuntimeException(
                    "Le declenchement manuel est reserve aux taches CONTINU ou A_LA_DEMANDE"
            );
        }

        // For manual trigger: set prochaineOccurrence to today and mark as terminee
        source.setStatut(StatutTache.TERMINEE);
        source.setDateFinReelle(LocalDate.now());
        source.setDateRealisee(LocalDate.now());
        source.setPourcentage(100);
        source.setProchaineOccurrence(LocalDate.now());
        tacheRepository.save(source);

        return creerNouvelleOccurrence(source);
    }
}

