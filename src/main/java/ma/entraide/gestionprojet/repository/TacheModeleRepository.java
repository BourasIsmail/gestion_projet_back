package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.TacheModele;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacheModeleRepository extends JpaRepository<TacheModele, Long> {

    List<TacheModele> findByTypeProjetIdOrderByOrdreAsc(Long typeProjetId);

    void deleteByTypeProjetId(Long typeProjetId);
}
