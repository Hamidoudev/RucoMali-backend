package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.PinDtos.ChangementPinRequest;
import com.malirussia.transfer.dto.PinDtos.EtatPinResponse;
import com.malirussia.transfer.dto.PinDtos.PinRequest;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.PinService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pin")
public class PinController {

    private final PinService pinService;
    private final UtilisateurRepository utilisateurRepository;

    public PinController(PinService pinService, UtilisateurRepository utilisateurRepository) {
        this.pinService = pinService;
        this.utilisateurRepository = utilisateurRepository;
    }

    private Utilisateur utilisateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    /** GET /api/pin/etat : le code PIN existe-t-il, est-il bloqué ? */
    @GetMapping("/etat")
    public EtatPinResponse etat() {
        return pinService.etat(utilisateurConnecte().getId());
    }

    /** POST /api/pin : crée le code PIN (une seule fois). */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void definir(@Valid @RequestBody PinRequest requete) {
        pinService.definir(utilisateurConnecte().getId(), requete.pin);
    }

    /** PUT /api/pin : change le code PIN, en donnant l'ancien. */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changer(@Valid @RequestBody ChangementPinRequest requete) {
        pinService.changer(utilisateurConnecte().getId(), requete.ancienPin, requete.nouveauPin);
    }
}
