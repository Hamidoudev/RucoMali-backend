package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.TransfertDtos.TransfertRequest;
import com.malirussia.transfer.dto.TransfertDtos.TransfertResponse;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.TransfertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transferts")
public class TransfertController {

    private final TransfertService transfertService;
    private final UtilisateurRepository utilisateurRepository;

    public TransfertController(TransfertService transfertService, UtilisateurRepository utilisateurRepository) {
        this.transfertService = transfertService;
        this.utilisateurRepository = utilisateurRepository;
    }

    private Utilisateur utilisateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    /** POST /api/transferts : envoie de l'argent à un bénéficiaire, confirmé par le code PIN. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransfertResponse creer(@Valid @RequestBody TransfertRequest requete) {
        return transfertService.creer(utilisateurConnecte(), requete);
    }

    /** GET /api/transferts?limite=20 : mes derniers transferts. */
    @GetMapping
    public List<TransfertResponse> lister(@RequestParam(defaultValue = "20") int limite) {
        return transfertService.lister(utilisateurConnecte(), Math.max(1, Math.min(limite, 100)));
    }

    /** GET /api/transferts/{reference} : le suivi d'un transfert. */
    @GetMapping("/{reference}")
    public TransfertResponse lire(@PathVariable String reference) {
        return transfertService.lire(utilisateurConnecte(), reference);
    }
}
