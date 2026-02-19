package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.RoleProjet;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "projet_membres",
        uniqueConstraints = @UniqueConstraint(columnNames = {"projet_id", "user_id"}))
public class ProjetMembre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_projet", nullable = false, length = 20)
    private RoleProjet roleProjet;

    @CreationTimestamp
    @Column(name = "date_ajout", updatable = false)
    private LocalDateTime dateAjout;

    public ProjetMembre() {
    }

    public ProjetMembre(Long id, Projet projet, User user, RoleProjet roleProjet, LocalDateTime dateAjout) {
        this.id = id;
        this.projet = projet;
        this.user = user;
        this.roleProjet = roleProjet;
        this.dateAjout = dateAjout;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Projet getProjet() { return projet; }
    public void setProjet(Projet projet) { this.projet = projet; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RoleProjet getRoleProjet() { return roleProjet; }
    public void setRoleProjet(RoleProjet roleProjet) { this.roleProjet = roleProjet; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
}

