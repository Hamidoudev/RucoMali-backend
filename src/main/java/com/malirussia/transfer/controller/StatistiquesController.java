package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.StatistiquesDtos.StatistiquesResponse;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.StatistiquesService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;
    private final UtilisateurRepository utilisateurRepository;

    public StatistiquesController(StatistiquesService statistiquesService, UtilisateurRepository utilisateurRepository) {
        this.statistiquesService = statistiquesService;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** GET /api/statistiques?jours=30 : mes envois sur la période (7, 30 ou 90 jours). */
    @GetMapping
    public StatistiquesResponse statistiques(@RequestParam(defaultValue = "30") int jours) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username).orElseThrow();
        return statistiquesService.calculer(utilisateur, Math.max(1, Math.min(jours, 365)));
    }
}
