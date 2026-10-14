package com.malirussia.transfer.service;

import com.malirussia.transfer.model.Devise;
import com.malirussia.transfer.model.TauxDeChange;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.TauxDeChangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Récupère les taux de change auprès d'une API publique (open.er-api.com, sans clé)
 * et enregistre le taux de chaque paire de devises actives.
 *
 * L'API est mise à jour une fois par jour et exige une mention de la source
 * ("Rates By Exchange Rate API", lien vers https://www.exchangerate-api.com) dans l'interface.
 */
@Service
public class TauxPubliqueService {

    private static final Logger log = LoggerFactory.getLogger(TauxPubliqueService.class);

    private final DeviseRepository deviseRepository;
    private final TauxDeChangeRepository tauxRepository;
    private final RestClient restClient;
    private final String url;

    public TauxPubliqueService(
        DeviseRepository deviseRepository,
        TauxDeChangeRepository tauxRepository,
        @Value("${app.taux.url:https://open.er-api.com/v6/latest/USD}") String url
    ) {
        this.deviseRepository = deviseRepository;
        this.tauxRepository = tauxRepository;
        this.url = url;

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory fabrique = new JdkClientHttpRequestFactory(httpClient);
        fabrique.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(fabrique).build();
    }

    /** Premier appel 5 secondes après le démarrage, puis toutes les 6 heures par défaut. */
    @Scheduled(initialDelay = 5000, fixedRateString = "${app.taux.rafraichissement-ms:21600000}")
    public void rafraichir() {
        try {
            Map<String, Object> reponse = restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (reponse == null || !"success".equals(reponse.get("result"))
                || !(reponse.get("rates") instanceof Map<?, ?> tauxParDevise)) {
                log.warn("Taux de change : réponse inattendue de l'API publique, anciens taux conservés.");
                return;
            }

            // Les taux de l'API sont exprimés par rapport au dollar (USD = 1).
            List<Devise> devises = deviseRepository.findByActiveTrueOrderByCodeAsc();
            LocalDateTime maintenant = LocalDateTime.now();
            List<TauxDeChange> nouveaux = new ArrayList<>();

            for (Devise source : devises) {
                BigDecimal enUsdSource = lireTaux(tauxParDevise, source.getCode());
                if (enUsdSource == null || enUsdSource.signum() <= 0) continue;

                for (Devise cible : devises) {
                    if (source.getCode().equals(cible.getCode())) continue;
                    BigDecimal enUsdCible = lireTaux(tauxParDevise, cible.getCode());
                    if (enUsdCible == null) continue;

                    BigDecimal taux = enUsdCible.divide(enUsdSource, 8, RoundingMode.HALF_UP);
                    TauxDeChange t = new TauxDeChange(source, cible, taux, TauxDeChange.Source.API_PUBLIQUE);
                    t.setDateMiseAJour(maintenant);
                    nouveaux.add(t);
                }
            }

            tauxRepository.saveAll(nouveaux);
            log.info("Taux de change mis à jour : {} paires enregistrées.", nouveaux.size());
        } catch (Exception e) {
            log.warn("Taux de change : impossible de contacter l'API publique ({}), anciens taux conservés.", e.getMessage());
        }
    }

    private BigDecimal lireTaux(Map<?, ?> tauxParDevise, String code) {
        Object valeur = tauxParDevise.get(code);
        if (valeur instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        return null;
    }
}
