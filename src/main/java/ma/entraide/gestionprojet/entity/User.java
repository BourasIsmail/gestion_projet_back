package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.RoleGlobal;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_global", nullable = false, length = 20)
    private RoleGlobal roleGlobal;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipeMembre> equipeMembres = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjetMembre> projetMembres = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TacheAssignee> tacheAssignees = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    public User() {
    }

    public User(Long id, String nom, String prenom, String email, String password,
                RoleGlobal roleGlobal, String avatarUrl, Boolean actif,
                LocalDateTime dateCreation, LocalDateTime dateMaj,
                List<EquipeMembre> equipeMembres, List<ProjetMembre> projetMembres,
                List<TacheAssignee> tacheAssignees, List<Notification> notifications) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.roleGlobal = roleGlobal;
        this.avatarUrl = avatarUrl;
        this.actif = actif;
        this.dateCreation = dateCreation;
        this.dateMaj = dateMaj;
        this.equipeMembres = equipeMembres;
        this.projetMembres = projetMembres;
        this.tacheAssignees = tacheAssignees;
        this.notifications = notifications;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RoleGlobal getRoleGlobal() { return roleGlobal; }
    public void setRoleGlobal(RoleGlobal roleGlobal) { this.roleGlobal = roleGlobal; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public List<EquipeMembre> getEquipeMembres() { return equipeMembres; }
    public void setEquipeMembres(List<EquipeMembre> equipeMembres) { this.equipeMembres = equipeMembres; }

    public List<ProjetMembre> getProjetMembres() { return projetMembres; }
    public void setProjetMembres(List<ProjetMembre> projetMembres) { this.projetMembres = projetMembres; }

    public List<TacheAssignee> getTacheAssignees() { return tacheAssignees; }
    public void setTacheAssignees(List<TacheAssignee> tacheAssignees) { this.tacheAssignees = tacheAssignees; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}

