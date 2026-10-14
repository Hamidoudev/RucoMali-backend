package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.DemandeDtos.DemandeRequest;
import com.malirussia.transfer.dto.DemandeDtos.DemandeResponse;
import com.malirussia.transfer.model.DemandeDeTransfert;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.DemandeDeTransfertRepository;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.MatchingService;
import com.malirussia.transfer.service.TauxDeChangeService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeDeTransfertRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TauxDeChangeService tauxDeChangeService;
    private final MatchingService matchingService;

    public DemandeController(
        DemandeDeTransfertRepository demandeRepository,
        UtilisateurRepository utilisateurRepository,
        TauxDeChangeService tauxDeChangeService,
        MatchingService matchingService
    ) {
        this.demandeRepository = demandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.tauxDeChangeService = tauxDeChangeService;
        this.matchingService = matchingService;
    }

    private Utilisateur utilisateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    /** GET /api/demandes : les demandes de l'utilisateur connecté. */
    @GetMapping
    public List<DemandeResponse> mesDemandes() {
        return demandeRepository.findByAuteurOrderByDateCreationDesc(utilisateurConnecte())
            .stream().map(DemandeResponse::depuis).toList();
    }

    /** GET /api/demandes/en-attente : toutes les demandes en attente (visibilité publique). */
    @GetMapping("/en-attente")
    public List<DemandeResponse> demandesEnAttente() {
        return demandeRepository.findByStatutOrderByDateCreationDesc(DemandeDeTransfert.Statut.EN_ATTENTE)
            .stream().map(DemandeResponse::depuis).toList();
    }

    /** POST /api/demandes : crée une demande, calcule un taux indicatif, tente un match immédiat. */
    @PostMapping
    public DemandeResponse creerDemande(@Valid @RequestBody DemandeRequest requete) {
        DemandeDeTransfert demande = new DemandeDeTransfert();
        demande.setAuteur(utilisateurConnecte());
        demande.setSens(DemandeDeTransfert.Sens.valueOf(requete.sens));
        demande.setMontantDeviseSource(requete.montantDeviseSource);
        demande.setDeviseSource(requete.deviseSource);
        demande.setDeviseCible(requete.deviseCible);
        demande.setTauxIndicatif(tauxDeChangeService.obtenirTaux(requete.deviseSource, requete.deviseCible));

        demandeRepository.save(demande);
        matchingService.creerMatchSiPossible(demande);

        return DemandeResponse.depuis(demande);
    }
}
