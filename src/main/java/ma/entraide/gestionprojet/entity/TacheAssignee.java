package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.RoleTache;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tache_assignees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tache_id", "user_id"}))
public class TacheAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_tache", nullable = false, length = 15)
    private RoleTache roleTache;

    @CreationTimestamp
    @Column(name = "date_ajout", updatable = false)
    private LocalDateTime dateAjout;

    public TacheAssignee() {
    }

    public TacheAssignee(Long id, Tache tache, User user, RoleTache roleTache, LocalDateTime dateAjout) {
        this.id = id;
        this.tache = tache;
        this.user = user;
        this.roleTache = roleTache;
        this.dateAjout = dateAjout;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tache getTache() { return tache; }
    public void setTache(Tache tache) { this.tache = tache; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RoleTache getRoleTache() { return roleTache; }
    public void setRoleTache(RoleTache roleTache) { this.roleTache = roleTache; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
}

