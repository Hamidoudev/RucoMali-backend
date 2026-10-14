package com.malirussia.transfer.service;

import com.malirussia.transfer.model.DemandeDeTransfert;
import com.malirussia.transfer.model.Match;
import com.malirussia.transfer.repository.DemandeDeTransfertRepository;
import com.malirussia.transfer.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Moteur de matching : le morceau le plus "recherche" du projet.
 *
 * Principe : pour une demande Mali→Russie, on cherche parmi les demandes
 * Russie→Mali en attente celle dont le montant à envoyer est le plus proche
 * (dans une tolérance donnée) du montant que la première demande veut
 * recevoir. Bon axe d'évolution pour ton rapport : transformer ça en un
 * vrai problème d'appariement biparti (plusieurs candidats simultanément,
 * minimisation de l'écart total sur l'ensemble des demandes en attente).
 */
@Service
public class MatchingService {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.05"); // 5 %

    private final DemandeDeTransfertRepository demandeRepository;
    private final MatchRepository matchRepository;

    public MatchingService(DemandeDeTransfertRepository demandeRepository, MatchRepository matchRepository) {
        this.demandeRepository = demandeRepository;
        this.matchRepository = matchRepository;
    }

    private DemandeDeTransfert.Sens sensOppose(DemandeDeTransfert.Sens sens) {
        return sens == DemandeDeTransfert.Sens.MALI_RU
            ? DemandeDeTransfert.Sens.RU_MALI
            : DemandeDeTransfert.Sens.MALI_RU;
    }

    private Optional<DemandeDeTransfert> trouverCandidat(DemandeDeTransfert demande) {
        BigDecimal cible = demande.getMontantDeviseCibleEstime();
        if (cible == null || cible.signum() == 0) return Optional.empty();

        List<DemandeDeTransfert> candidates = demandeRepository.findBySensAndStatut(
            sensOppose(demande.getSens()), DemandeDeTransfert.Statut.EN_ATTENTE
        );

        DemandeDeTransfert meilleur = null;
        BigDecimal plusPetitEcart = null;

        for (DemandeDeTransfert candidate : candidates) {
            if (candidate.getId().equals(demande.getId())) continue;
            BigDecimal source = candidate.getMontantDeviseSource();
            if (source == null) continue;

            BigDecimal ecart = cible.subtract(source).abs().divide(cible, 6, java.math.RoundingMode.HALF_UP);
            if (ecart.compareTo(TOLERANCE) <= 0) {
                if (plusPetitEcart == null || ecart.compareTo(plusPetitEcart) < 0) {
                    plusPetitEcart = ecart;
                    meilleur = candidate;
                }
            }
        }

        return Optional.ofNullable(meilleur);
    }

    /** Essaie de matcher une demande fraîchement créée. Retourne le Match créé, s'il y en a un. */
    public Optional<Match> creerMatchSiPossible(DemandeDeTransfert demande) {
        Optional<DemandeDeTransfert> candidatOpt = trouverCandidat(demande);
        if (candidatOpt.isEmpty()) return Optional.empty();

        DemandeDeTransfert candidate = candidatOpt.get();
        DemandeDeTransfert demandeMali = demande.getSens() == DemandeDeTransfert.Sens.MALI_RU ? demande : candidate;
        DemandeDeTransfert demandeRussie = demande.getSens() == DemandeDeTransfert.Sens.MALI_RU ? candidate : demande;

        Match match = new Match(demandeMali, demandeRussie);
        matchRepository.save(match);

        demande.setStatut(DemandeDeTransfert.Statut.MATCHEE);
        candidate.setStatut(DemandeDeTransfert.Statut.MATCHEE);
        demandeRepository.save(demande);
        demandeRepository.save(candidate);

        return Optional.of(match);
    }
}
