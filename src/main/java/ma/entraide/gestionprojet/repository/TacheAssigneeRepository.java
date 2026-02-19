package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.TacheAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TacheAssigneeRepository extends JpaRepository<TacheAssignee, Long> {

    Optional<TacheAssignee> findByTacheIdAndUserId(Long tacheId, Long userId);

    List<TacheAssignee> findByTacheId(Long tacheId);

    List<TacheAssignee> findByUserId(Long userId);

    boolean existsByTacheIdAndUserId(Long tacheId, Long userId);

    void deleteByTacheIdAndUserId(Long tacheId, Long userId);
}

