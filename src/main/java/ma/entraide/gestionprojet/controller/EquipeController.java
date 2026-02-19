package ma.entraide.gestionprojet.controller;

import jakarta.validation.Valid;
import ma.entraide.gestionprojet.dto.EquipeDTO;
import ma.entraide.gestionprojet.dto.request.AddMembreRequest;
import ma.entraide.gestionprojet.dto.request.CreateEquipeRequest;
import ma.entraide.gestionprojet.security.UserDetailsImpl;
import ma.entraide.gestionprojet.service.EquipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeService equipeService;

    public EquipeController(EquipeService equipeService) {
        this.equipeService = equipeService;
    }

    @GetMapping
    public ResponseEntity<List<EquipeDTO>> getAllEquipes() {
        return ResponseEntity.ok(equipeService.getAllEquipes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeDTO> getEquipeById(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.getEquipeById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<EquipeDTO> createEquipe(
            @Valid @RequestBody CreateEquipeRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipeService.createEquipe(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<EquipeDTO> updateEquipe(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(equipeService.updateEquipe(id, body.get("nom"), body.get("description")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiveEquipe(@PathVariable Long id) {
        equipeService.archiveEquipe(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/membres")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<Void> addMembre(@PathVariable Long id, @Valid @RequestBody AddMembreRequest request) {
        equipeService.addMembre(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/membres/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<Void> removeMembre(@PathVariable Long id, @PathVariable Long userId) {
        equipeService.removeMembre(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/membres/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<Void> changeRoleMembre(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        equipeService.changeRoleMembre(id, userId, body.get("role"));
        return ResponseEntity.noContent().build();
    }
}

