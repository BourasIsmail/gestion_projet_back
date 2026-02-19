package ma.entraide.gestionprojet.rapport.controller;

import ma.entraide.gestionprojet.rapport.service.*;
import ma.entraide.gestionprojet.security.UserDetailsImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/rapports")
public class RapportController {

    private final RapportProjetService rapportProjetService;
    private final RapportEquipeService rapportEquipeService;
    private final RapportAlertesService rapportAlertesService;
    private final RapportUserService rapportUserService;
    private final RapportGlobalService rapportGlobalService;

    public RapportController(RapportProjetService rapportProjetService,
                             RapportEquipeService rapportEquipeService,
                             RapportAlertesService rapportAlertesService,
                             RapportUserService rapportUserService,
                             RapportGlobalService rapportGlobalService) {
        this.rapportProjetService = rapportProjetService;
        this.rapportEquipeService = rapportEquipeService;
        this.rapportAlertesService = rapportAlertesService;
        this.rapportUserService = rapportUserService;
        this.rapportGlobalService = rapportGlobalService;
    }

    @GetMapping("/projet/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> rapportProjet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        byte[] pdf = rapportProjetService.genererRapportProjet(id, user.getNomComplet());
        return buildPdfResponse(pdf, "rapport-projet-" + id);
    }

    @GetMapping("/projet/{id}/taches")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> rapportTachesProjet(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @AuthenticationPrincipal UserDetailsImpl user) {
        byte[] pdf = rapportProjetService.genererRapportTaches(id, user.getNomComplet(), dateDebut, dateFin);
        return buildPdfResponse(pdf, "rapport-taches-projet-" + id);
    }

    @GetMapping("/equipe/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<byte[]> rapportEquipe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        byte[] pdf = rapportEquipeService.genererRapportEquipe(id, user.getNomComplet());
        return buildPdfResponse(pdf, "rapport-equipe-" + id);
    }

    @GetMapping("/equipe/{id}/projets")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<byte[]> rapportProjetsEquipe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        byte[] pdf = rapportEquipeService.genererRapportProjetsEquipe(id, user.getNomComplet());
        return buildPdfResponse(pdf, "rapport-projets-equipe-" + id);
    }

    @GetMapping("/alertes")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_EQUIPE')")
    public ResponseEntity<byte[]> rapportAlertes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @AuthenticationPrincipal UserDetailsImpl user) {
        byte[] pdf = rapportAlertesService.genererRapportAlertes(user.getNomComplet(), dateDebut, dateFin);
        return buildPdfResponse(pdf, "rapport-alertes");
    }

    @GetMapping("/utilisateur/{id}/activite")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> rapportActiviteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        byte[] pdf = rapportUserService.genererRapportUser(id, user.getNomComplet());
        return buildPdfResponse(pdf, "rapport-activite-user-" + id);
    }

    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> rapportGlobal(
            @AuthenticationPrincipal UserDetailsImpl user) {
        byte[] pdf = rapportGlobalService.genererRapportGlobal(user.getNomComplet());
        return buildPdfResponse(pdf, "rapport-global");
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String fileNamePrefix) {
        String fileName = fileNamePrefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}

