package ma.entraide.gestionprojet.repository;

import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.entity.enums.RoleGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActifTrue();

    List<User> findByRoleGlobal(RoleGlobal role);

    long countByActifTrue();
}

