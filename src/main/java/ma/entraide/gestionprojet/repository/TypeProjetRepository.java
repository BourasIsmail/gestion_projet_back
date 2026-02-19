package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.TypeProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeProjetRepository extends JpaRepository<TypeProjet, Long> {

    Optional<TypeProjet> findByCode(String code);

    List<TypeProjet> findByActifTrue();

    boolean existsByCode(String code);
}
