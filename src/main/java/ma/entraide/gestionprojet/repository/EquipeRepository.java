package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    List<Equipe> findByActifTrue();

    @Query("SELECT e FROM Equipe e JOIN e.membres m WHERE m.user.id = :userId AND e.actif = true")
    List<Equipe> findByMembreUserId(@Param("userId") Long userId);

    boolean existsByNom(String nom);

    long countByActifTrue();
}
