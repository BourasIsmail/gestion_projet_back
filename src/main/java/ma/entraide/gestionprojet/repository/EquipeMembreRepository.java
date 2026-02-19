package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.EquipeMembre;
import ma.entraide.gestionprojet.entity.enums.RoleEquipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipeMembreRepository extends JpaRepository<EquipeMembre, Long> {

    Optional<EquipeMembre> findByEquipeIdAndUserId(Long equipeId, Long userId);

    List<EquipeMembre> findByEquipeId(Long equipeId);

    List<EquipeMembre> findByUserId(Long userId);

    boolean existsByEquipeIdAndUserId(Long equipeId, Long userId);

    boolean existsByEquipeIdAndUserIdAndRoleEquipe(Long equipeId, Long userId, RoleEquipe role);

    void deleteByEquipeIdAndUserId(Long equipeId, Long userId);
}
