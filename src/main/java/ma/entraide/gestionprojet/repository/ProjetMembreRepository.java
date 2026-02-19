package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.ProjetMembre;
import ma.entraide.gestionprojet.entity.enums.RoleProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetMembreRepository extends JpaRepository<ProjetMembre, Long> {

    Optional<ProjetMembre> findByProjetIdAndUserId(Long projetId, Long userId);

    List<ProjetMembre> findByProjetId(Long projetId);

    List<ProjetMembre> findByUserId(Long userId);

    boolean existsByProjetIdAndUserId(Long projetId, Long userId);

    boolean existsByProjetIdAndUserIdAndRoleProjet(Long projetId, Long userId, RoleProjet role);

    void deleteByProjetIdAndUserId(Long projetId, Long userId);
}

