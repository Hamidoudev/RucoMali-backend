package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.MatchDtos.MatchResponse;
import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.DemandeDeTransfert;
import com.malirussia.transfer.model.Match;
import com.malirussia.transfer.model.Transaction;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.DemandeDeTransfertRepository;
import com.malirussia.transfer.repository.MatchRepository;
import com.malirussia.transfer.repository.TransactionRepository;
import com.malirussia.transfer.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchRepository matchRepository;
    private final DemandeDeTransfertRepository demandeRepository;
    private final TransactionRepository transactionRepository;
    private final UtilisateurRepository utilisateurRepository;

    public MatchController(
        MatchRepository matchRepository,
        DemandeDeTransfertRepository demandeRepository,
        TransactionRepository transactionRepository,
        UtilisateurRepository utilisateurRepository
    ) {
        this.matchRepository = matchRepository;
        this.demandeRepository = demandeRepository;
        this.transactionRepository = transactionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    private Utilisateur utilisateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    /** GET /api/matches : les matches impliquant l'utilisateur connecté. */
    @GetMapping
    public List<MatchResponse> mesMatches() {
        Long id = utilisateurConnecte().getId();
        return matchRepository.findTousPourUtilisateur(id).stream().map(MatchResponse::depuis).toList();
    }

    /** POST /api/matches/{id}/confirmer : l'utilisateur confirme avoir payé/reçu localement. */
    @PostMapping("/{id}/confirmer")
    public MatchResponse confirmer(@PathVariable Long id) {
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "erreur.match.introuvable"));
        Utilisateur utilisateur = utilisateurConnecte();

        if (match.getDemandeMaliVersRussie().getAuteur().getId().equals(utilisateur.getId())) {
            match.setConfirmeParMali(true);
        } else if (match.getDemandeRussieVersMali().getAuteur().getId().equals(utilisateur.getId())) {
            match.setConfirmeParRussie(true);
        } else {
            throw new ApiException(HttpStatus.FORBIDDEN, "erreur.hors.match");
        }
        matchRepository.save(match);

        if (match.getEstComplet() && !transactionRepository.existsByMatchId(match.getId())) {
            Transaction transaction = new Transaction(match, match.getDemandeMaliVersRussie().getTauxIndicatif());
            transactionRepository.save(transaction);

            match.getDemandeMaliVersRussie().setStatut(DemandeDeTransfert.Statut.COMPLETE);
            match.getDemandeRussieVersMali().setStatut(DemandeDeTransfert.Statut.COMPLETE);
            demandeRepository.save(match.getDemandeMaliVersRussie());
            demandeRepository.save(match.getDemandeRussieVersMali());
        }

        return MatchResponse.depuis(match);
    }
}
