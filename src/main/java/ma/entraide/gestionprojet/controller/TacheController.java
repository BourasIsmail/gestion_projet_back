package ma.entraide.gestionprojet.controller;

import jakarta.validation.Valid;
import ma.entraide.gestionprojet.dto.TacheDTO;
import ma.entraide.gestionprojet.dto.request.AddMembreRequest;
import ma.entraide.gestionprojet.dto.request.CreateTacheRequest;
import ma.entraide.gestionprojet.dto.request.UpdateTacheRequest;
import ma.entraide.gestionprojet.security.UserDetailsImpl;
import ma.entraide.gestionprojet.service.CommentaireService;
import ma.entraide.gestionprojet.service.TacheRecurrenteService;
import ma.entraide.gestionprojet.service.TacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taches")
public class TacheController {

    private final TacheService tacheService;
    private final TacheRecurrenteService tacheRecurrenteService;
    private final CommentaireService commentaireService;

    public TacheController(TacheService tacheService,
                           TacheRecurrenteService tacheRecurrenteService,
                           CommentaireService commentaireService) {
        this.tacheService = tacheService;
        this.tacheRecurrenteService = tacheRecurrenteService;
        this.commentaireService = commentaireService;
    }

    @GetMapping
    public ResponseEntity<List<TacheDTO>> getTaches(
            @RequestParam(required = false) Long projetId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (projetId != null) {
            return ResponseEntity.ok(tacheService.getTachesByProjet(projetId));
        }
        return ResponseEntity.ok(tacheService.getTachesByUser(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TacheDTO> getTacheById(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.getTacheById(id));
    }

    @PostMapping
    public ResponseEntity<TacheDTO> createTache(
            @Valid @RequestBody CreateTacheRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tacheService.createTache(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TacheDTO> updateTache(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTacheRequest request) {
        return ResponseEntity.ok(tacheService.updateTache(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        tacheService.deleteTache(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<TacheDTO> changeStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(tacheService.changeStatut(id, body.get("statut")));
    }

    @PutMapping("/{id}/avancement")
    public ResponseEntity<TacheDTO> updateAvancement(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(tacheService.updateAvancement(id, body.get("pourcentage")));
    }

    @PostMapping("/{id}/assignees")
    public ResponseEntity<Void> assignUser(
            @PathVariable Long id,
            @Valid @RequestBody AddMembreRequest request) {
        tacheService.assignUser(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/assignees/{userId}")
    public ResponseEntity<Void> unassignUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        tacheService.unassignUser(id, userId);
        return ResponseEntity.noContent().build();
    }

    // --- Recurrence endpoints ---

    @GetMapping("/recurrentes")
    public ResponseEntity<List<TacheDTO>> getAllTachesRecurrentes() {
        return ResponseEntity.ok(tacheService.getAllTachesRecurrentes());
    }

    @GetMapping("/recurrentes/projet/{projetId}")
    public ResponseEntity<List<TacheDTO>> getTachesRecurrentesByProjet(@PathVariable Long projetId) {
        return ResponseEntity.ok(tacheService.getTachesRecurrentesByProjet(projetId));
    }

    @GetMapping("/{id}/occurrences")
    public ResponseEntity<List<TacheDTO>> getOccurrenceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.getOccurrenceHistory(id));
    }

    @PostMapping("/{id}/generer-occurrence")
    public ResponseEntity<TacheDTO> genererOccurrenceManuellement(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TacheDTO.fromEntity(tacheRecurrenteService.genererOccurrenceManuellement(id)));
    }

    // --- Commentaires ---

    @GetMapping("/{id}/commentaires")
    public ResponseEntity<List<CommentaireService.CommentaireDTO>> getCommentaires(@PathVariable Long id) {
        return ResponseEntity.ok(commentaireService.getCommentairesByTache(id));
    }

    @PostMapping("/{id}/commentaires")
    public ResponseEntity<CommentaireService.CommentaireDTO> addCommentaire(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentaireService.addCommentaireToTache(id, body.get("contenu"), currentUser.getId()));
    }
}

