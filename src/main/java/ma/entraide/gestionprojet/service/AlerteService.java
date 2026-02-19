package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.TacheAssignee;
import ma.entraide.gestionprojet.entity.enums.ReferenceType;
import ma.entraide.gestionprojet.entity.enums.TypeNotification;
import ma.entraide.gestionprojet.repository.TacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlerteService {

    private static final Logger log = LoggerFactory.getLogger(AlerteService.class);

    private final TacheRepository tacheRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${app.alerts.approach-days:3}")
    private int approachDays;

    @Value("${app.alerts.urgent-days:1}")
    private int urgentDays;

    public AlerteService(TacheRepository tacheRepository,
                         NotificationService notificationService,
                         EmailService emailService) {
        this.tacheRepository = tacheRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Job quotidien : verification des deadlines a 08h00 chaque jour.
     */
    @Scheduled(cron = "${app.alerts.cron-daily}")
    public void verifierDeadlinesQuotidien() {
        log.info("Demarrage de la verification quotidienne des deadlines...");
        LocalDate today = LocalDate.now();

        verifierApproche(today.plusDays(approachDays), "Approche deadline (J-" + approachDays + ")");
        verifierApproche(today.plusDays(urgentDays), "Deadline demain (J-" + urgentDays + ")");
        verifierRetards(today);

        log.info("Verification quotidienne des deadlines terminee.");
    }

    /**
     * Job hebdomadaire : resume des retards chaque lundi a 09h00.
     */
    @Scheduled(cron = "${app.alerts.cron-weekly}")
    public void resumeHebdomadaire() {
        log.info("Generation du resume hebdomadaire des retards...");
        LocalDate today = LocalDate.now();
        List<Tache> tachesEnRetard = tacheRepository.findAllTachesEnRetard(today);

        if (!tachesEnRetard.isEmpty()) {
            String resume = genererResumeRetards(tachesEnRetard);
            log.info("Resume hebdomadaire : {} taches en retard", tachesEnRetard.size());
            emailService.envoyerResumeHebdomadaire(resume, tachesEnRetard.size());
        }
    }

    private void verifierApproche(LocalDate dateEcheance, String label) {
        List<Tache> taches = tacheRepository.findTachesApprochantDeadline(dateEcheance);

        for (Tache tache : taches) {
            long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), tache.getDateEcheance());
            String titre = label + " : " + tache.getTitre();
            String message = String.format(
                    "La tache '%s' du projet '%s' arrive a echeance dans %d jour(s) (le %s).",
                    tache.getTitre(),
                    tache.getProjet().getNom(),
                    joursRestants,
                    tache.getDateEcheance()
            );

            for (TacheAssignee assignee : tache.getAssignees()) {
                notificationService.creerNotification(
                        assignee.getUser(), titre, message,
                        TypeNotification.ALERTE_APPROCHE,
                        ReferenceType.TACHE, tache.getId()
                );
                emailService.envoyerAlerteApproche(assignee.getUser(), tache, joursRestants);
            }

            tache.setAlerteEnvoyeeApproche(true);
            tacheRepository.save(tache);
        }
    }

    private void verifierRetards(LocalDate today) {
        List<Tache> tachesEnRetard = tacheRepository.findTachesEnRetardNonAlertees(today);

        for (Tache tache : tachesEnRetard) {
            long joursRetard = ChronoUnit.DAYS.between(tache.getDateEcheance(), today);
            String titre = "RETARD : " + tache.getTitre();
            String message = String.format(
                    "La tache '%s' du projet '%s' est en retard de %d jour(s) (deadline : %s).",
                    tache.getTitre(),
                    tache.getProjet().getNom(),
                    joursRetard,
                    tache.getDateEcheance()
            );

            for (TacheAssignee assignee : tache.getAssignees()) {
                notificationService.creerNotification(
                        assignee.getUser(), titre, message,
                        TypeNotification.ALERTE_RETARD,
                        ReferenceType.TACHE, tache.getId()
                );
                emailService.envoyerAlerteRetard(assignee.getUser(), tache, joursRetard);
            }

            tache.setAlerteEnvoyeeRetard(true);
            tacheRepository.save(tache);
        }
    }

    private String genererResumeRetards(List<Tache> taches) {
        return taches.stream()
                .map(t -> String.format("- %s (Projet: %s, Retard: %d jours, Priorite: %s)",
                        t.getTitre(),
                        t.getProjet().getNom(),
                        t.getJoursRetard(),
                        t.getPriorite().name()))
                .collect(Collectors.joining("\n"));
    }
}

