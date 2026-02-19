package ma.entraide.gestionprojet.controller;

import ma.entraide.gestionprojet.dto.DashboardDTO;
import ma.entraide.gestionprojet.dto.TacheDTO;
import ma.entraide.gestionprojet.security.UserDetailsImpl;
import ma.entraide.gestionprojet.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardDTO.StatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/mes-taches")
    public ResponseEntity<List<TacheDTO>> getMesTaches(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(dashboardService.getMesTachesEnCours(currentUser.getId()));
    }

    @GetMapping("/alertes")
    public ResponseEntity<DashboardDTO> getAlertes(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(dashboardService.getDashboard(currentUser.getId()));
    }
}

