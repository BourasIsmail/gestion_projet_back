package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.ReferenceType;
import ma.entraide.gestionprojet.entity.enums.TypeNotification;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 300)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TypeNotification type;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 10)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(nullable = false)
    private Boolean lue = false;

    @Column(name = "email_envoye", nullable = false)
    private Boolean emailEnvoye = false;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    public Notification() {
    }

    public Notification(Long id, User user, String titre, String message, TypeNotification type,
                        ReferenceType referenceType, Long referenceId, Boolean lue,
                        Boolean emailEnvoye, LocalDateTime dateCreation) {
        this.id = id;
        this.user = user;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.lue = lue;
        this.emailEnvoye = emailEnvoye;
        this.dateCreation = dateCreation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public TypeNotification getType() { return type; }
    public void setType(TypeNotification type) { this.type = type; }

    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public Boolean getLue() { return lue; }
    public void setLue(Boolean lue) { this.lue = lue; }

    public Boolean getEmailEnvoye() { return emailEnvoye; }
    public void setEmailEnvoye(Boolean emailEnvoye) { this.emailEnvoye = emailEnvoye; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}

