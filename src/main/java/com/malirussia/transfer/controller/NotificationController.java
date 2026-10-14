package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.NotificationDtos.NotificationsResponse;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    public NotificationController(NotificationService notificationService, UtilisateurRepository utilisateurRepository) {
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    private Utilisateur utilisateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    /** GET /api/notifications?limite=20 : les dernières notifications et le nombre de non lues. */
    @GetMapping
    public NotificationsResponse lister(@RequestParam(defaultValue = "20") int limite) {
        return notificationService.lister(utilisateurConnecte().getId(), Math.max(1, Math.min(limite, 100)));
    }

    /** POST /api/notifications/lues : marque toutes les notifications comme lues. */
    @PostMapping("/lues")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void toutMarquerLu() {
        notificationService.toutMarquerLu(utilisateurConnecte().getId());
    }
}
