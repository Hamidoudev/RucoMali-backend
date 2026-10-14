package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.DevisDtos.ComparaisonDevis;
import com.malirussia.transfer.service.DevisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/devis")
public class DevisController {

    private final DevisService devisService;

    public DevisController(DevisService devisService) {
        this.devisService = devisService;
    }

    /** GET /api/devis?source=RUB&cible=XOF&montant=50000 : le montant reçu en mode match et en mode coffre. */
    @GetMapping
    public ComparaisonDevis devis(
        @RequestParam String source,
        @RequestParam String cible,
        @RequestParam BigDecimal montant
    ) {
        ComparaisonDevis resultat = new ComparaisonDevis();
        resultat.match = devisService.calculer(source, cible, montant, DevisService.Mode.MATCH);
        resultat.coffre = devisService.calculer(source, cible, montant, DevisService.Mode.COFFRE);
        return resultat;
    }
}
