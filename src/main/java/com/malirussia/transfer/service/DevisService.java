package com.malirussia.transfer.service;

import com.malirussia.transfer.dto.DevisDtos.DevisResponse;
import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.Devise;
import com.malirussia.transfer.model.TauxDeChange;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.TauxDeChangeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Calcule ce que le destinataire recevra.
 *  - Mode MATCH : taux de référence, sans marge.
 *  - Mode COFFRE : taux de référence diminué de la marge de RucoMali.
 * Les frais (en pourcentage du montant envoyé) sont affichés séparément, jamais cachés dans le taux.
 */
@Service
public class DevisService {

    public enum Mode { MATCH, COFFRE }

    private final DeviseRepository deviseRepository;
    private final TauxDeChangeRepository tauxRepository;
    private final BigDecimal fraisPourcent;
    private final BigDecimal margeCoffrePourcent;
    private final long validiteMinutes;

    public DevisService(
        DeviseRepository deviseRepository,
        TauxDeChangeRepository tauxRepository,
        @Value("${app.devis.frais-pourcent:1.0}") BigDecimal fraisPourcent,
        @Value("${app.devis.marge-coffre-pourcent:2.0}") BigDecimal margeCoffrePourcent,
        @Value("${app.devis.validite-minutes:10}") long validiteMinutes
    ) {
        this.deviseRepository = deviseRepository;
        this.tauxRepository = tauxRepository;
        this.fraisPourcent = fraisPourcent;
        this.margeCoffrePourcent = margeCoffrePourcent;
        this.validiteMinutes = validiteMinutes;
    }

    public DevisResponse calculer(String codeSource, String codeCible, BigDecimal montant, Mode mode) {
        String source = codeSource == null ? "" : codeSource.trim().toUpperCase();
        String cible = codeCible == null ? "" : codeCible.trim().toUpperCase();

        if (source.equals(cible)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.devises.identiques");
        }
        if (montant == null || montant.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.montant.invalide");
        }

        Devise deviseSource = deviseRepository.findById(source).filter(Devise::isActive)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "erreur.devise.inconnue"));
        Devise deviseCible = deviseRepository.findById(cible).filter(Devise::isActive)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "erreur.devise.inconnue"));

        TauxDeChange taux = tauxRepository
            .findFirstByDeviseSource_CodeAndDeviseCible_CodeOrderByDateMiseAJourDesc(source, cible)
            .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "erreur.taux.indisponible"));

        BigDecimal tauxReference = taux.getTaux();
        BigDecimal facteur = mode == Mode.COFFRE
            ? BigDecimal.ONE.subtract(margeCoffrePourcent.movePointLeft(2))
            : BigDecimal.ONE;
        BigDecimal tauxApplique = tauxReference.multiply(facteur).setScale(8, RoundingMode.HALF_UP);

        BigDecimal montantEnvoye = montant.setScale(deviseSource.getDecimales(), RoundingMode.HALF_UP);
        BigDecimal frais = montantEnvoye.multiply(fraisPourcent).movePointLeft(2)
            .setScale(deviseSource.getDecimales(), RoundingMode.HALF_UP);
        BigDecimal net = montantEnvoye.subtract(frais);
        if (net.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.montant.trop.faible");
        }
        BigDecimal montantRecu = net.multiply(tauxApplique)
            .setScale(deviseCible.getDecimales(), RoundingMode.HALF_UP);

        DevisResponse r = new DevisResponse();
        r.deviseSource = source;
        r.deviseCible = cible;
        r.mode = mode.name();
        r.montantEnvoye = montantEnvoye;
        r.frais = frais;
        r.montantRecu = montantRecu;
        r.tauxReference = tauxReference;
        r.tauxApplique = tauxApplique;
        r.dateTaux = taux.getDateMiseAJour();
        r.expireLe = LocalDateTime.now().plusMinutes(validiteMinutes);
        return r;
    }
}
