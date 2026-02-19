package ma.entraide.gestionprojet.service;

import ma.entraide.gestionprojet.entity.Commentaire;
import ma.entraide.gestionprojet.entity.Tache;
import ma.entraide.gestionprojet.entity.User;
import ma.entraide.gestionprojet.repository.CommentaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final TacheService tacheService;
    private final ProjetService projetService;
    private final UserService userService;

    public CommentaireService(CommentaireRepository commentaireRepository,
                              TacheService tacheService,
                              ProjetService projetService,
                              UserService userService) {
        this.commentaireRepository = commentaireRepository;
        this.tacheService = tacheService;
        this.projetService = projetService;
        this.userService = userService;
    }

    public record CommentaireDTO(Long id, String contenu, String auteurNom, Long auteurId, LocalDateTime dateCreation) {}

    public List<CommentaireDTO> getCommentairesByTache(Long tacheId) {
        return commentaireRepository.findByTacheIdOrderByDateCreationDesc(tacheId).stream()
                .map(c -> new CommentaireDTO(c.getId(), c.getContenu(), c.getUser().getNomComplet(), c.getUser().getId(), c.getDateCreation()))
                .toList();
    }

    public CommentaireDTO addCommentaireToTache(Long tacheId, String contenu, Long userId) {
        Tache tache = tacheService.findTacheOrThrow(tacheId);
        User user = userService.findUserOrThrow(userId);

        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(contenu);
        commentaire.setTache(tache);
        commentaire.setUser(user);
        commentaire = commentaireRepository.save(commentaire);
        return new CommentaireDTO(commentaire.getId(), commentaire.getContenu(),
                user.getNomComplet(), user.getId(), commentaire.getDateCreation());
    }

    public List<CommentaireDTO> getCommentairesByProjet(Long projetId) {
        return commentaireRepository.findByProjetIdOrderByDateCreationDesc(projetId).stream()
                .map(c -> new CommentaireDTO(c.getId(), c.getContenu(), c.getUser().getNomComplet(), c.getUser().getId(), c.getDateCreation()))
                .toList();
    }
}

