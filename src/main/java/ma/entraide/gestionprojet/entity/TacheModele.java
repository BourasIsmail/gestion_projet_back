package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;
import ma.entraide.gestionprojet.entity.enums.Priorite;

@Entity
@Table(name = "taches_modeles")
public class TacheModele {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_projet_id", nullable = false)
    private TypeProjet typeProjet;

    @Column(nullable = false, length = 300)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priorite priorite;

    @Column(nullable = false)
    private Integer ordre = 0;

    @Column(name = "delai_jours")
    private Integer delaiJours;

    public TacheModele() {
    }

    public TacheModele(Long id, TypeProjet typeProjet, String titre, String description,
                       Priorite priorite, Integer ordre, Integer delaiJours) {
        this.id = id;
        this.typeProjet = typeProjet;
        this.titre = titre;
        this.description = description;
        this.priorite = priorite;
        this.ordre = ordre;
        this.delaiJours = delaiJours;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypeProjet getTypeProjet() { return typeProjet; }
    public void setTypeProjet(TypeProjet typeProjet) { this.typeProjet = typeProjet; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public Integer getDelaiJours() { return delaiJours; }
    public void setDelaiJours(Integer delaiJours) { this.delaiJours = delaiJours; }
}

