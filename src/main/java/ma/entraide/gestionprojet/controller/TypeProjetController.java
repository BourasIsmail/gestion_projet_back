package ma.entraide.gestionprojet.controller;

import ma.entraide.gestionprojet.dto.TypeProjetDTO;
import ma.entraide.gestionprojet.service.TypeProjetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/types-projet")
public class TypeProjetController {

    private final TypeProjetService typeProjetService;

    public TypeProjetController(TypeProjetService typeProjetService) {
        this.typeProjetService = typeProjetService;
    }

    @GetMapping
    public ResponseEntity<List<TypeProjetDTO>> getAllTypes() {
        return ResponseEntity.ok(typeProjetService.getAllTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeProjetDTO> getTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(typeProjetService.getTypeById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypeProjetDTO> createType(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(typeProjetService.createType(body.get("code"), body.get("libelle"), body.get("description")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypeProjetDTO> updateType(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(typeProjetService.updateType(id, body.get("libelle"), body.get("description")));
    }

    @GetMapping("/{id}/taches-modeles")
    public ResponseEntity<List<TypeProjetDTO.TacheModeleDTO>> getTachesModeles(@PathVariable Long id) {
        return ResponseEntity.ok(typeProjetService.getTachesModeles(id));
    }

    @PostMapping("/{id}/taches-modeles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypeProjetDTO.TacheModeleDTO> addTacheModele(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(typeProjetService.addTacheModele(
                        id,
                        (String) body.get("titre"),
                        (String) body.get("description"),
                        (String) body.get("priorite"),
                        body.get("ordre") != null ? ((Number) body.get("ordre")).intValue() : null,
                        body.get("delaiJours") != null ? ((Number) body.get("delaiJours")).intValue() : null
                ));
    }

    @PutMapping("/taches-modeles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypeProjetDTO.TacheModeleDTO> updateTacheModele(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(typeProjetService.updateTacheModele(
                id,
                (String) body.get("titre"),
                (String) body.get("description"),
                (String) body.get("priorite"),
                body.get("ordre") != null ? ((Number) body.get("ordre")).intValue() : null,
                body.get("delaiJours") != null ? ((Number) body.get("delaiJours")).intValue() : null
        ));
    }

    @DeleteMapping("/taches-modeles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTacheModele(@PathVariable Long id) {
        typeProjetService.deleteTacheModele(id);
        return ResponseEntity.noContent().build();
    }
}

