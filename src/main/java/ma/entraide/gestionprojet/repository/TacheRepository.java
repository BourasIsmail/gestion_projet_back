package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByProjetId(Long projetId);

    void deleteByProjetId(Long projetId);

    List<Tache> findByProjetIdAndTacheParentIsNull(Long projetId);

    List<Tache> findByStatut(StatutTache statut);

    @Query("SELECT t FROM Tache t JOIN t.assignees a WHERE a.user.id = :userId")
    List<Tache> findByAssigneeUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Tache t JOIN t.assignees a WHERE a.user.id = :userId " +
            "AND t.statut NOT IN ('TERMINEE') ORDER BY t.dateEcheance ASC")
    List<Tache> findActiveTachesByUserId(@Param("userId") Long userId);

    // Alertes : taches proches de la deadline
    @Query("SELECT t FROM Tache t WHERE t.dateEcheance = :date " +
            "AND t.statut NOT IN ('TERMINEE') AND t.alerteEnvoyeeApproche = false")
    List<Tache> findTachesApprochantDeadline(@Param("date") LocalDate date);

    // Alertes : taches en retard
    @Query("SELECT t FROM Tache t WHERE t.dateEcheance < :date " +
            "AND t.statut NOT IN ('TERMINEE') AND t.alerteEnvoyeeRetard = false")
    List<Tache> findTachesEnRetardNonAlertees(@Param("date") LocalDate date);

    // Toutes les taches en retard (pour dashboard)
    @Query("SELECT t FROM Tache t WHERE t.dateEcheance < :date " +
            "AND t.statut NOT IN ('TERMINEE') ORDER BY t.priorite DESC, t.dateEcheance ASC")
    List<Tache> findAllTachesEnRetard(@Param("date") LocalDate date);

    // Taches approchant deadline dans les N prochains jours
    @Query("SELECT t FROM Tache t WHERE t.dateEcheance BETWEEN :debut AND :fin " +
            "AND t.statut NOT IN ('TERMINEE') ORDER BY t.dateEcheance ASC")
    List<Tache> findTachesProchesDeadline(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    long countByStatut(StatutTache statut);

    long countByProjetId(Long projetId);

    @Query("SELECT COUNT(t) FROM Tache t WHERE t.dateEcheance < CURRENT_DATE AND t.statut NOT IN ('TERMINEE')")
    long countTachesEnRetard();

    // Recurrence: taches recurrentes terminees qui ont besoin d'une nouvelle occurrence
    @Query("SELECT t FROM Tache t WHERE t.estRecurrente = true AND t.statut = 'TERMINEE' " +
            "AND t.prochaineOccurrence IS NOT NULL AND t.prochaineOccurrence <= :date")
    List<Tache> findTachesRecurrentesARegenerer(@Param("date") LocalDate date);

    // Recurrence: taches recurrentes planifiees dont la date est arrivee
    @Query("SELECT t FROM Tache t WHERE t.estRecurrente = true AND t.statut = 'PLANIFIEE' " +
            "AND t.dateDebut IS NOT NULL AND t.dateDebut <= :date")
    List<Tache> findTachesPlanifieesAActiver(@Param("date") LocalDate date);

    // Toutes les taches recurrentes actives d'un projet
    @Query("SELECT t FROM Tache t WHERE t.projet.id = :projetId AND t.estRecurrente = true " +
            "ORDER BY t.periodicite, t.titre")
    List<Tache> findTachesRecurrentesByProjet(@Param("projetId") Long projetId);

    // Toutes les taches recurrentes actives globalement
    @Query("SELECT t FROM Tache t WHERE t.estRecurrente = true AND t.statut NOT IN ('TERMINEE') " +
            "ORDER BY t.prochaineOccurrence ASC NULLS LAST")
    List<Tache> findAllTachesRecurrentes();

    // Historique des occurrences d'une tache recurrente parent
    @Query("SELECT t FROM Tache t WHERE t.tacheRecurrenteParent.id = :parentId " +
            "ORDER BY t.occurrenceNumero DESC")
    List<Tache> findOccurrencesByParent(@Param("parentId") Long parentId);
}
