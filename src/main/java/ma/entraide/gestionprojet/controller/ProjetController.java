package ma.entraide.gestionprojet.controller;

import jakarta.validation.Valid;
import ma.entraide.gestionprojet.dto.ProjetDTO;
import ma.entraide.gestionprojet.dto.request.AddMembreRequest;
import ma.entraide.gestionprojet.dto.request.CreateProjetRequest;
import ma.entraide.gestionprojet.dto.request.UpdateProjetRequest;
import ma.entraide.gestionprojet.security.UserDetailsImpl;
import ma.entraide.gestionprojet.service.ProjetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final ProjetService projetService;

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    @GetMapping
    public ResponseEntity<List<ProjetDTO>> getAllProjets(
            @RequestParam(required = false) Long equipeId) {
        if (equipeId != null) {
            return ResponseEntity.ok(projetService.getProjetsByEquipe(equipeId));
        }
        return ResponseEntity.ok(projetService.getAllProjets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetDTO> getProjetById(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.getProjetById(id));
    }

    @PostMapping
    public ResponseEntity<ProjetDTO> createProjet(
            @Valid @RequestBody CreateProjetRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projetService.createProjet(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetDTO> updateProjet(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjetRequest request) {
        return ResponseEntity.ok(projetService.updateProjet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveProjet(@PathVariable Long id) {
        projetService.archiveProjet(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/membres")
    public ResponseEntity<Void> addMembre(
            @PathVariable Long id,
            @Valid @RequestBody AddMembreRequest request) {
        projetService.addMembre(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/membres/{userId}")
    public ResponseEntity<Void> removeMembre(
            @PathVariable Long id,
            @PathVariable Long userId) {
        projetService.removeMembre(id, userId);
        return ResponseEntity.noContent().build();
    }
}

