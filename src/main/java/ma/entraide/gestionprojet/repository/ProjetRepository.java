package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.Projet;
import ma.entraide.gestionprojet.entity.enums.Priorite;
import ma.entraide.gestionprojet.entity.enums.StatutProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    List<Projet> findByEquipeId(Long equipeId);

    List<Projet> findByStatut(StatutProjet statut);

    List<Projet> findByPriorite(Priorite priorite);

    @Query("SELECT p FROM Projet p JOIN p.membres m WHERE m.user.id = :userId")
    List<Projet> findByMembreUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Projet p WHERE p.equipe.id IN :equipeIds")
    List<Projet> findByEquipeIdIn(@Param("equipeIds") List<Long> equipeIds);

    long countByStatut(StatutProjet statut);

    @Query("SELECT p FROM Projet p WHERE p.statut NOT IN ('TERMINE', 'ANNULE')")
    List<Projet> findActifs();
}

