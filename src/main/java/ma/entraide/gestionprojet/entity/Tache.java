package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.Periodicite;
import ma.entraide.gestionprojet.entity.enums.Priorite;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taches")
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_projet_id")
    private TypeProjet typeProjet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_parent_id")
    private Tache tacheParent;

    @OneToMany(mappedBy = "tacheParent", cascade = CascadeType.ALL)
    private List<Tache> sousTaches = new ArrayList<>();

    @Column(name = "est_modele", nullable = false)
    private Boolean estModele = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priorite priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatutTache statut = StatutTache.A_FAIRE;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_echeance", nullable = false)
    private LocalDate dateEcheance;

    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;

    @Column(nullable = false)
    private Integer pourcentage = 0;

    // --- Champs de recurrence ---

    @Column(name = "est_recurrente", nullable = false)
    private Boolean estRecurrente = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite", length = 20)
    private Periodicite periodicite;

    @Column(name = "regle_recurrence", length = 500)
    private String regleRecurrence;

    @Column(name = "duree_estimee_heures")
    private Double dureeEstimeeHeures;

    @Column(name = "date_realisee")
    private LocalDate dateRealisee;

    @Column(name = "prochaine_occurrence")
    private LocalDate prochaineOccurrence;

    @Column(name = "occurrence_numero")
    private Integer occurrenceNumero = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_recurrente_parent_id")
    private Tache tacheRecurrenteParent;

    // --- Champs d'alerte ---

    @Column(name = "alerte_envoyee_approche", nullable = false)
    private Boolean alerteEnvoyeeApproche = false;

    @Column(name = "alerte_envoyee_retard", nullable = false)
    private Boolean alerteEnvoyeeRetard = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TacheAssignee> assignees = new ArrayList<>();

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentaire> commentaires = new ArrayList<>();

    public Tache() {
    }

    public Tache(Long id, String titre, String description, Projet projet, TypeProjet typeProjet,
                 Tache tacheParent, List<Tache> sousTaches, Boolean estModele, Priorite priorite,
                 StatutTache statut, LocalDate dateDebut, LocalDate dateEcheance, LocalDate dateFinReelle,
                 Integer pourcentage, Boolean estRecurrente, Periodicite periodicite,
                 String regleRecurrence, Double dureeEstimeeHeures, LocalDate dateRealisee,
                 LocalDate prochaineOccurrence, Integer occurrenceNumero,
                 Tache tacheRecurrenteParent, Boolean alerteEnvoyeeApproche,
                 Boolean alerteEnvoyeeRetard, User createdBy, LocalDateTime dateCreation,
                 LocalDateTime dateMaj, List<TacheAssignee> assignees, List<Commentaire> commentaires) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.projet = projet;
        this.typeProjet = typeProjet;
        this.tacheParent = tacheParent;
        this.sousTaches = sousTaches;
        this.estModele = estModele;
        this.priorite = priorite;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateEcheance = dateEcheance;
        this.dateFinReelle = dateFinReelle;
        this.pourcentage = pourcentage;
        this.estRecurrente = estRecurrente;
        this.periodicite = periodicite;
        this.regleRecurrence = regleRecurrence;
        this.dureeEstimeeHeures = dureeEstimeeHeures;
        this.dateRealisee = dateRealisee;
        this.prochaineOccurrence = prochaineOccurrence;
        this.occurrenceNumero = occurrenceNumero;
        this.tacheRecurrenteParent = tacheRecurrenteParent;
        this.alerteEnvoyeeApproche = alerteEnvoyeeApproche;
        this.alerteEnvoyeeRetard = alerteEnvoyeeRetard;
        this.createdBy = createdBy;
        this.dateCreation = dateCreation;
        this.dateMaj = dateMaj;
        this.assignees = assignees;
        this.commentaires = commentaires;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Projet getProjet() { return projet; }
    public void setProjet(Projet projet) { this.projet = projet; }

    public TypeProjet getTypeProjet() { return typeProjet; }
    public void setTypeProjet(TypeProjet typeProjet) { this.typeProjet = typeProjet; }

    public Tache getTacheParent() { return tacheParent; }
    public void setTacheParent(Tache tacheParent) { this.tacheParent = tacheParent; }

    public List<Tache> getSousTaches() { return sousTaches; }
    public void setSousTaches(List<Tache> sousTaches) { this.sousTaches = sousTaches; }

    public Boolean getEstModele() { return estModele; }
    public void setEstModele(Boolean estModele) { this.estModele = estModele; }

    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }

    public StatutTache getStatut() { return statut; }
    public void setStatut(StatutTache statut) { this.statut = statut; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public LocalDate getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDate dateFinReelle) { this.dateFinReelle = dateFinReelle; }

    public Integer getPourcentage() { return pourcentage; }
    public void setPourcentage(Integer pourcentage) { this.pourcentage = pourcentage; }

    public Boolean getAlerteEnvoyeeApproche() { return alerteEnvoyeeApproche; }
    public void setAlerteEnvoyeeApproche(Boolean alerteEnvoyeeApproche) { this.alerteEnvoyeeApproche = alerteEnvoyeeApproche; }

    public Boolean getAlerteEnvoyeeRetard() { return alerteEnvoyeeRetard; }
    public void setAlerteEnvoyeeRetard(Boolean alerteEnvoyeeRetard) { this.alerteEnvoyeeRetard = alerteEnvoyeeRetard; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public List<TacheAssignee> getAssignees() { return assignees; }
    public void setAssignees(List<TacheAssignee> assignees) { this.assignees = assignees; }

    public List<Commentaire> getCommentaires() { return commentaires; }
    public void setCommentaires(List<Commentaire> commentaires) { this.commentaires = commentaires; }

    public Boolean getEstRecurrente() { return estRecurrente; }
    public void setEstRecurrente(Boolean estRecurrente) { this.estRecurrente = estRecurrente; }

    public Periodicite getPeriodicite() { return periodicite; }
    public void setPeriodicite(Periodicite periodicite) { this.periodicite = periodicite; }

    public String getRegleRecurrence() { return regleRecurrence; }
    public void setRegleRecurrence(String regleRecurrence) { this.regleRecurrence = regleRecurrence; }

    public Double getDureeEstimeeHeures() { return dureeEstimeeHeures; }
    public void setDureeEstimeeHeures(Double dureeEstimeeHeures) { this.dureeEstimeeHeures = dureeEstimeeHeures; }

    public LocalDate getDateRealisee() { return dateRealisee; }
    public void setDateRealisee(LocalDate dateRealisee) { this.dateRealisee = dateRealisee; }

    public LocalDate getProchaineOccurrence() { return prochaineOccurrence; }
    public void setProchaineOccurrence(LocalDate prochaineOccurrence) { this.prochaineOccurrence = prochaineOccurrence; }

    public Integer getOccurrenceNumero() { return occurrenceNumero; }
    public void setOccurrenceNumero(Integer occurrenceNumero) { this.occurrenceNumero = occurrenceNumero; }

    public Tache getTacheRecurrenteParent() { return tacheRecurrenteParent; }
    public void setTacheRecurrenteParent(Tache tacheRecurrenteParent) { this.tacheRecurrenteParent = tacheRecurrenteParent; }

    public boolean isEnRetard() {
        return dateEcheance != null
                && LocalDate.now().isAfter(dateEcheance)
                && statut != StatutTache.TERMINEE;
    }

    public long getJoursRetard() {
        if (!isEnRetard()) return 0;
        return ChronoUnit.DAYS.between(dateEcheance, LocalDate.now());
    }
}

