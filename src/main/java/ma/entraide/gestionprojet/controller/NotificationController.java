package ma.entraide.gestionprojet.controller;

import ma.entraide.gestionprojet.dto.NotificationDTO;
import ma.entraide.gestionprojet.security.UserDetailsImpl;
import ma.entraide.gestionprojet.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(currentUser.getId(), pageable));
    }

    @GetMapping("/non-lues/count")
    public ResponseEntity<Map<String, Long>> countNonLues(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(Map.of("count", notificationService.countNonLues(currentUser.getId())));
    }

    @PutMapping("/{id}/lue")
    public ResponseEntity<Void> marquerCommeLue(@PathVariable Long id) {
        notificationService.marquerCommeLue(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tout-lire")
    public ResponseEntity<Void> marquerToutesCommeLues(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        notificationService.marquerToutesCommeLues(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

