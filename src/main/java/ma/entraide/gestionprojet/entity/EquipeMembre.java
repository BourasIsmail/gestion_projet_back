package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.RoleEquipe;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipe_membres",
        uniqueConstraints = @UniqueConstraint(columnNames = {"equipe_id", "user_id"}))
public class EquipeMembre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_equipe", nullable = false, length = 20)
    private RoleEquipe roleEquipe;

    @CreationTimestamp
    @Column(name = "date_ajout", updatable = false)
    private LocalDateTime dateAjout;

    public EquipeMembre() {
    }

    public EquipeMembre(Long id, Equipe equipe, User user, RoleEquipe roleEquipe, LocalDateTime dateAjout) {
        this.id = id;
        this.equipe = equipe;
        this.user = user;
        this.roleEquipe = roleEquipe;
        this.dateAjout = dateAjout;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RoleEquipe getRoleEquipe() { return roleEquipe; }
    public void setRoleEquipe(RoleEquipe roleEquipe) { this.roleEquipe = roleEquipe; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
}

