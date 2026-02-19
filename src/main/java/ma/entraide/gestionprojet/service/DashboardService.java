package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.dto.DashboardDTO;
import ma.entraide.gestionprojet.dto.TacheDTO;
import ma.entraide.gestionprojet.entity.enums.StatutProjet;
import ma.entraide.gestionprojet.entity.enums.StatutTache;
import ma.entraide.gestionprojet.repository.EquipeRepository;
import ma.entraide.gestionprojet.repository.ProjetRepository;
import ma.entraide.gestionprojet.repository.TacheRepository;
import ma.entraide.gestionprojet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final EquipeRepository equipeRepository;
    private final UserRepository userRepository;

    public DashboardService(ProjetRepository projetRepository,
                            TacheRepository tacheRepository,
                            EquipeRepository equipeRepository,
                            UserRepository userRepository) {
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.equipeRepository = equipeRepository;
        this.userRepository = userRepository;
    }

    public DashboardDTO getDashboard(Long userId) {
        DashboardDTO.StatsDTO stats = getStats();
        List<TacheDTO> mesTaches = getMesTachesEnCours(userId);
        List<TacheDTO> enRetard = getTachesEnRetard();
        List<TacheDTO> prochesDeadline = getTachesProchesDeadline();

        return new DashboardDTO(stats, mesTaches, enRetard, prochesDeadline);
    }

    public DashboardDTO.StatsDTO getStats() {
        return new DashboardDTO.StatsDTO(
                projetRepository.count(),
                projetRepository.countByStatut(StatutProjet.EN_COURS),
                projetRepository.countByStatut(StatutProjet.TERMINE),
                tacheRepository.count(),
                tacheRepository.countByStatut(StatutTache.TERMINEE),
                tacheRepository.countTachesEnRetard(),
                equipeRepository.countByActifTrue(),
                userRepository.countByActifTrue()
        );
    }

    public List<TacheDTO> getMesTachesEnCours(Long userId) {
        return tacheRepository.findActiveTachesByUserId(userId).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public List<TacheDTO> getTachesEnRetard() {
        return tacheRepository.findAllTachesEnRetard(LocalDate.now()).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }

    public List<TacheDTO> getTachesProchesDeadline() {
        LocalDate today = LocalDate.now();
        return tacheRepository.findTachesProchesDeadline(today, today.plusDays(3)).stream()
                .map(TacheDTO::fromEntity)
                .toList();
    }
}

