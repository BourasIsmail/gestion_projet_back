package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.ActionType;
import ma.entraide.gestionprojet.entity.enums.EntiteType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_activite")
public class HistoriqueActivite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ActionType action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entite_type", nullable = false, length = 10)
    private EntiteType entiteType;

    @Column(name = "entite_id", nullable = false)
    private Long entiteId;

    @Column(columnDefinition = "JSON")
    private String details;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    public HistoriqueActivite() {
    }

    public HistoriqueActivite(Long id, User user, ActionType action, EntiteType entiteType,
                              Long entiteId, String details, LocalDateTime dateCreation) {
        this.id = id;
        this.user = user;
        this.action = action;
        this.entiteType = entiteType;
        this.entiteId = entiteId;
        this.details = details;
        this.dateCreation = dateCreation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }

    public EntiteType getEntiteType() { return entiteType; }
    public void setEntiteType(EntiteType entiteType) { this.entiteType = entiteType; }

    public Long getEntiteId() { return entiteId; }
    public void setEntiteId(Long entiteId) { this.entiteId = entiteId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}

