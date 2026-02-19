package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@gestionprojets.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerAlerteApproche(User destinataire, Tache tache, long joursRestants) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinataire.getEmail());
            message.setSubject("[Alerte] Deadline approche : " + tache.getTitre());
            message.setText(String.format(
                    "Bonjour %s,\n\n" +
                            "La tache '%s' du projet '%s' arrive a echeance dans %d jour(s).\n" +
                            "Date d'echeance : %s\n" +
                            "Priorite : %s\n\n" +
                            "Veuillez prendre les mesures necessaires.\n\n" +
                            "Cordialement,\nSysteme de Gestion de Projets",
                    destinataire.getPrenom(),
                    tache.getTitre(),
                    tache.getProjet().getNom(),
                    joursRestants,
                    tache.getDateEcheance(),
                    tache.getPriorite().name()
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'alerte approche a {}: {}",
                    destinataire.getEmail(), e.getMessage());
        }
    }

    public void envoyerAlerteRetard(User destinataire, Tache tache, long joursRetard) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinataire.getEmail());
            message.setSubject("[URGENT] Tache en retard : " + tache.getTitre());
            message.setText(String.format(
                    "Bonjour %s,\n\n" +
                            "ATTENTION : La tache '%s' du projet '%s' est en retard de %d jour(s).\n" +
                            "Date d'echeance depassee : %s\n" +
                            "Priorite : %s\n\n" +
                            "Action immediate requise.\n\n" +
                            "Cordialement,\nSysteme de Gestion de Projets",
                    destinataire.getPrenom(),
                    tache.getTitre(),
                    tache.getProjet().getNom(),
                    joursRetard,
                    tache.getDateEcheance(),
                    tache.getPriorite().name()
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'alerte retard a {}: {}",
                    destinataire.getEmail(), e.getMessage());
        }
    }

    public void envoyerNotificationNouvelleOccurrence(User destinataire, Tache tache) {
        try {
            String periodiciteLabel = tache.getPeriodicite() != null
                    ? tache.getPeriodicite().name().toLowerCase().replace('_', ' ')
                    : "recurrente";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinataire.getEmail());
            message.setSubject("[Recurrence] Nouvelle occurrence : " + tache.getTitre());
            message.setText(String.format(
                    "Bonjour %s,\n\n" +
                            "La tache recurrente (%s) '%s' du projet '%s' a genere une nouvelle occurrence (#%d).\n" +
                            "Date de debut : %s\n" +
                            "Date d'echeance : %s\n" +
                            "Priorite : %s\n\n" +
                            "Veuillez traiter cette tache dans les delais impartis.\n\n" +
                            "Cordialement,\nSysteme de Gestion de Projets",
                    destinataire.getPrenom(),
                    periodiciteLabel,
                    tache.getTitre(),
                    tache.getProjet().getNom(),
                    tache.getOccurrenceNumero() != null ? tache.getOccurrenceNumero() : 1,
                    tache.getDateDebut(),
                    tache.getDateEcheance(),
                    tache.getPriorite().name()
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de nouvelle occurrence a {}: {}",
                    destinataire.getEmail(), e.getMessage());
        }
    }

    public void envoyerResumeHebdomadaire(String resume, int count) {
        try {
            log.info("Resume hebdomadaire des retards ({} taches):\n{}", count, resume);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du resume hebdomadaire: {}", e.getMessage());
        }
    }
}

