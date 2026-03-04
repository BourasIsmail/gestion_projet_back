package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.entity.PieceJointe;
import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.exception.ResourceNotFoundException;
import ma.entraide.gestionprojet.repository.PieceJointeRepository;
import ma.entraide.gestionprojet.repository.TacheRepository;
import ma.entraide.gestionprojet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PieceJointeService {

    private final PieceJointeRepository pieceJointeRepository;
    private final TacheRepository tacheRepository;
    private final UserRepository userRepository;

    public PieceJointeService(PieceJointeRepository pieceJointeRepository,
                              TacheRepository tacheRepository,
                              UserRepository userRepository) {
        this.pieceJointeRepository = pieceJointeRepository;
        this.tacheRepository = tacheRepository;
        this.userRepository = userRepository;
    }

    public record PieceJointeDTO(
            Long id,
            String nomOriginal,
            String typeMime,
            Long tailleFichier,
            String uploadedByNom,
            LocalDateTime dateUpload
    ) {
        public static PieceJointeDTO fromEntity(PieceJointe pj) {
            return new PieceJointeDTO(
                    pj.getId(),
                    pj.getNomOriginal(),
                    pj.getTypeMime(),
                    pj.getTailleFichier(),
                    pj.getUploadedBy().getNomComplet(),
                    pj.getDateUpload()
            );
        }
    }

    public List<PieceJointeDTO> getPiecesJointesByTache(Long tacheId) {
        return pieceJointeRepository.findByTacheIdOrderByDateUploadDesc(tacheId)
                .stream()
                .map(PieceJointeDTO::fromEntity)
                .toList();
    }

    @Transactional
    public PieceJointeDTO uploadPieceJointe(Long tacheId, MultipartFile file, Long userId) throws IOException {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new ResourceNotFoundException("Tache non trouvee"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        PieceJointe pj = new PieceJointe();
        pj.setNomFichier(UUID.randomUUID().toString());
        pj.setNomOriginal(file.getOriginalFilename());
        pj.setTypeMime(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        pj.setTailleFichier(file.getSize());
        pj.setDonnees(file.getBytes());
        pj.setTache(tache);
        pj.setUploadedBy(user);

        PieceJointe saved = pieceJointeRepository.save(pj);
        return PieceJointeDTO.fromEntity(saved);
    }

    public PieceJointe getPieceJointe(Long id) {
        return pieceJointeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Piece jointe non trouvee"));
    }

    @Transactional
    public void deletePieceJointe(Long id) {
        PieceJointe pj = pieceJointeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Piece jointe non trouvee"));
        pieceJointeRepository.delete(pj);
    }
}

