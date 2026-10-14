package com.malirussia.transfer.service;

import com.malirussia.transfer.dto.StatistiquesDtos.PointJour;
import com.malirussia.transfer.dto.StatistiquesDtos.StatistiquesResponse;
import com.malirussia.transfer.model.Transfert;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.TauxDeChangeRepository;
import com.malirussia.transfer.repository.TransfertRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Les statistiques d'envoi d'un utilisateur sur une période, comparées à la période précédente de même durée.
 * Les montants sont convertis dans la monnaie de son pays, au dernier taux connu (valeurs indicatives).
 */
@Service
public class StatistiquesService {

    private final TransfertRepository transferts;
    private final TauxDeChangeRepository taux;
    private final DeviseRepository devises;

    public StatistiquesService(TransfertRepository transferts, TauxDeChangeRepository taux, DeviseRepository devises) {
        this.transferts = transferts;
        this.taux = taux;
        this.devises = devises;
    }

    public StatistiquesResponse calculer(Utilisateur utilisateur, int jours) {
        String reference = utilisateur.getPays() == Utilisateur.Pays.RUSSIE ? "RUB" : "XOF";
        int decimales = devises.findById(reference).map(d -> d.getDecimales()).orElse(2);

        LocalDate debut = LocalDate.now().minusDays(jours - 1L);
        LocalDate debutPrecedent = debut.minusDays(jours);
        List<Transfert> liste = transferts.findByExpediteurIdAndDateCreationGreaterThanEqual(
            utilisateur.getId(), debutPrecedent.atStartOfDay()
        );

        Map<LocalDate, BigDecimal> parJour = new TreeMap<>();
        for (int i = 0; i < jours; i++) parJour.put(debut.plusDays(i), BigDecimal.ZERO);

        Map<String, BigDecimal> cacheTaux = new HashMap<>();
        BigDecimal volume = BigDecimal.ZERO, volumePrecedent = BigDecimal.ZERO;
        BigDecimal frais = BigDecimal.ZERO, fraisPrecedents = BigDecimal.ZERO;
        long nombre = 0, nombrePrecedent = 0;

        for (Transfert t : liste) {
            LocalDate jour = t.getDateCreation().toLocalDate();
            BigDecimal v = convertir(t.getDeviseSource(), reference, t.getMontantSource(), cacheTaux);
            BigDecimal f = convertir(t.getDeviseSource(), reference, t.getFrais(), cacheTaux);
            if (!jour.isBefore(debut)) {
                parJour.merge(jour, v, BigDecimal::add);
                volume = volume.add(v);
                frais = frais.add(f);
                nombre++;
            } else {
                volumePrecedent = volumePrecedent.add(v);
                fraisPrecedents = fraisPrecedents.add(f);
                nombrePrecedent++;
            }
        }

        StatistiquesResponse r = new StatistiquesResponse();
        r.deviseReference = reference;
        r.decimalesReference = decimales;
        r.jours = jours;
        r.serie = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> e : parJour.entrySet()) {
            PointJour p = new PointJour();
            p.date = e.getKey();
            p.volume = e.getValue().setScale(decimales, RoundingMode.HALF_UP);
            r.serie.add(p);
        }
        r.volumeTotal = volume.setScale(decimales, RoundingMode.HALF_UP);
        r.volumePrecedent = volumePrecedent.setScale(decimales, RoundingMode.HALF_UP);
        r.fraisPayes = frais.setScale(decimales, RoundingMode.HALF_UP);
        r.fraisPrecedents = fraisPrecedents.setScale(decimales, RoundingMode.HALF_UP);
        r.nombreTransferts = nombre;
        r.nombreTransfertsPrecedent = nombrePrecedent;
        return r;
    }

    private BigDecimal convertir(String de, String vers, BigDecimal montant, Map<String, BigDecimal> cache) {
        if (de.equals(vers)) return montant;
        BigDecimal t = cache.computeIfAbsent(de, code ->
            taux.findFirstByDeviseSource_CodeAndDeviseCible_CodeOrderByDateMiseAJourDesc(code, vers)
                .map(x -> x.getTaux())
                .orElse(BigDecimal.ZERO)
        );
        return montant.multiply(t);
    }
}
