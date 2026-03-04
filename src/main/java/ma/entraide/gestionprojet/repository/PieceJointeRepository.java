package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {
    List<PieceJointe> findByTacheIdOrderByDateUploadDesc(Long tacheId);
}

