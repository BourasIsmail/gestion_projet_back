package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    List<Commentaire> findByTacheIdOrderByDateCreationDesc(Long tacheId);

    List<Commentaire> findByProjetIdOrderByDateCreationDesc(Long projetId);
}

