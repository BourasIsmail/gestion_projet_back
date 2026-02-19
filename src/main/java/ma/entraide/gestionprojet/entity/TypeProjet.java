package ma.entraide.gestionprojet.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "types_projet")
public class TypeProjet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean actif = true;

    @OneToMany(mappedBy = "typeProjet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    private List<TacheModele> tachesModeles = new ArrayList<>();

    public TypeProjet() {
    }

    public TypeProjet(Long id, String code, String libelle, String description, Boolean actif,
                      List<TacheModele> tachesModeles) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.actif = actif;
        this.tachesModeles = tachesModeles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public List<TacheModele> getTachesModeles() { return tachesModeles; }
    public void setTachesModeles(List<TacheModele> tachesModeles) { this.tachesModeles = tachesModeles; }
}

