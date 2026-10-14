package com.malirussia.transfer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Petit service pour récupérer un taux de change indicatif.
 * En cas d'échec réseau (fréquent avec les restrictions actuelles), retombe
 * sur une valeur par défaut codée en dur pour ne pas bloquer la démo.
 */
@Service
public class TauxDeChangeService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Map<String, BigDecimal> TAUX_DE_REPLI = Map.of(
        "XOF_RUB", new BigDecimal("0.135"),
        "RUB_XOF", new BigDecimal("7.4"),
        "EUR_RUB", new BigDecimal("95.0"),
        "RUB_EUR", new BigDecimal("0.0105")
    );

    @SuppressWarnings("unchecked")
    public BigDecimal obtenirTaux(String deviseSource, String deviseCible) {
        try {
            Map<String, Object> reponse = restTemplate.getForObject(
                "https://open.er-api.com/v6/latest/" + deviseSource, Map.class
            );
            if (reponse != null && reponse.get("rates") instanceof Map) {
                Map<String, Object> taux = (Map<String, Object>) reponse.get("rates");
                Object valeur = taux.get(deviseCible);
                if (valeur != null) {
                    return new BigDecimal(valeur.toString()).setScale(6, RoundingMode.HALF_UP);
                }
            }
        } catch (RestClientException e) {
            // Réseau injoignable : on retombe sur le taux de repli ci-dessous.
        }

        return TAUX_DE_REPLI.getOrDefault(deviseSource + "_" + deviseCible, BigDecimal.ONE);
    }
}
