package ma.entraide.gestionprojet.config;

import ma.entraide.gestionprojet.entity.TacheModele;
import ma.entraide.gestionprojet.entity.TypeProjet;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.entity.enums.Priorite;
import ma.entraide.gestionprojet.entity.enums.RoleGlobal;
import ma.entraide.gestionprojet.repository.TypeProjetRepository;
import ma.entraide.gestionprojet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TypeProjetRepository typeProjetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(TypeProjetRepository typeProjetRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.typeProjetRepository = typeProjetRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initTypesProjet();
        initAdminUser();
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail("admin@gestionprojets.com")) {
            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setEmail("admin@gestionprojets.com");
            admin.setPassword(passwordEncoder.encode("Admin@2026"));
            admin.setRoleGlobal(RoleGlobal.ADMIN);
            admin.setActif(true);
            userRepository.save(admin);
            log.info("Utilisateur admin cree : admin@gestionprojets.com / Admin@2026");
        }
    }

    private void initTypesProjet() {
        if (typeProjetRepository.count() > 0) return;

        // DEVELOPPEMENT
        TypeProjet dev = createType("DEVELOPPEMENT", "Developpement", "Projets de developpement logiciel");
        addModeles(dev, List.of(
                modele("Analyse des besoins", Priorite.HAUTE, 1, 5),
                modele("Redaction du cahier des charges", Priorite.HAUTE, 2, 10),
                modele("Conception technique", Priorite.HAUTE, 3, 15),
                modele("Mise en place de l'environnement de dev", Priorite.MOYENNE, 4, 8),
                modele("Developpement backend", Priorite.CRITIQUE, 5, 30),
                modele("Developpement frontend", Priorite.CRITIQUE, 6, 30),
                modele("Tests unitaires", Priorite.HAUTE, 7, 35),
                modele("Tests d'integration", Priorite.HAUTE, 8, 38),
                modele("Recette / UAT", Priorite.HAUTE, 9, 42),
                modele("Correction des anomalies", Priorite.MOYENNE, 10, 45),
                modele("Deploiement en production", Priorite.CRITIQUE, 11, 48),
                modele("Documentation technique", Priorite.MOYENNE, 12, 50)
        ));
        typeProjetRepository.save(dev);

        // RESEAUX_INFRA
        TypeProjet reseaux = createType("RESEAUX_INFRA", "Reseaux et Infra", "Projets reseaux, infrastructure, systemes");
        addModeles(reseaux, List.of(
                modele("Audit de l'infrastructure existante", Priorite.HAUTE, 1, 5),
                modele("Analyse des besoins reseau", Priorite.HAUTE, 2, 8),
                modele("Conception de l'architecture", Priorite.CRITIQUE, 3, 15),
                modele("Choix et commande du materiel", Priorite.HAUTE, 4, 20),
                modele("Configuration des equipements", Priorite.CRITIQUE, 5, 30),
                modele("Installation physique", Priorite.HAUTE, 6, 35),
                modele("Tests de connectivite", Priorite.HAUTE, 7, 38),
                modele("Configuration de la securite reseau", Priorite.CRITIQUE, 8, 40),
                modele("Tests de performance", Priorite.MOYENNE, 9, 42),
                modele("Migration des services", Priorite.CRITIQUE, 10, 48),
                modele("Validation et recette", Priorite.HAUTE, 11, 50),
                modele("Documentation reseau", Priorite.MOYENNE, 12, 52)
        ));
        typeProjetRepository.save(reseaux);

        // CYBERSECURITE
        TypeProjet cyber = createType("CYBERSECURITE", "Cybersecurite", "Projets securite, audit, conformite");
        addModeles(cyber, List.of(
                modele("Cadrage et perimetre d'audit", Priorite.HAUTE, 1, 3),
                modele("Inventaire des actifs", Priorite.HAUTE, 2, 7),
                modele("Analyse des risques", Priorite.CRITIQUE, 3, 12),
                modele("Scan de vulnerabilites", Priorite.CRITIQUE, 4, 15),
                modele("Tests d'intrusion (pentest)", Priorite.CRITIQUE, 5, 25),
                modele("Analyse des resultats", Priorite.HAUTE, 6, 28),
                modele("Redaction du rapport de securite", Priorite.HAUTE, 7, 32),
                modele("Plan de remediation", Priorite.CRITIQUE, 8, 35),
                modele("Implementation des correctifs", Priorite.CRITIQUE, 9, 45),
                modele("Verification post-remediation", Priorite.HAUTE, 10, 48),
                modele("Formation / sensibilisation", Priorite.MOYENNE, 11, 50),
                modele("Rapport final et recommandations", Priorite.HAUTE, 12, 52)
        ));
        typeProjetRepository.save(cyber);

        // DBA
        TypeProjet dba = createType("DBA", "Administration BDD", "Projets base de donnees, migration, optimisation");
        addModeles(dba, List.of(
                modele("Analyse de l'existant (schema, volumes)", Priorite.HAUTE, 1, 4),
                modele("Audit de performance BDD", Priorite.HAUTE, 2, 8),
                modele("Conception du nouveau schema", Priorite.CRITIQUE, 3, 15),
                modele("Plan de migration", Priorite.CRITIQUE, 4, 18),
                modele("Mise en place environnement de test", Priorite.HAUTE, 5, 22),
                modele("Script de migration", Priorite.CRITIQUE, 6, 28),
                modele("Tests de migration (env test)", Priorite.CRITIQUE, 7, 32),
                modele("Optimisation des index et requetes", Priorite.HAUTE, 8, 35),
                modele("Configuration backup / replication", Priorite.HAUTE, 9, 38),
                modele("Migration en production", Priorite.CRITIQUE, 10, 42),
                modele("Validation post-migration", Priorite.HAUTE, 11, 44),
                modele("Documentation BDD", Priorite.MOYENNE, 12, 46)
        ));
        typeProjetRepository.save(dba);

        // AUTRE
        TypeProjet autre = createType("AUTRE", "Autre", "Projets generiques sans taches par defaut");
        typeProjetRepository.save(autre);

        log.info("Types de projets et modeles de taches initialises avec succes");
    }

    private TypeProjet createType(String code, String libelle, String description) {
        TypeProjet type = new TypeProjet();
        type.setCode(code);
        type.setLibelle(libelle);
        type.setDescription(description);
        type.setActif(true);
        return type;
    }

    private TacheModele modele(String titre, Priorite priorite, int ordre, int delaiJours) {
        TacheModele modele = new TacheModele();
        modele.setTitre(titre);
        modele.setPriorite(priorite);
        modele.setOrdre(ordre);
        modele.setDelaiJours(delaiJours);
        return modele;
    }

    private void addModeles(TypeProjet type, List<TacheModele> modeles) {
        for (TacheModele modele : modeles) {
            modele.setTypeProjet(type);
            type.getTachesModeles().add(modele);
        }
    }
}

