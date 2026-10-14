package com.malirussia.transfer.service;

import com.malirussia.transfer.model.CompteWallet;
import com.malirussia.transfer.model.Devise;
import com.malirussia.transfer.model.EcritureWallet;
import com.malirussia.transfer.model.OperationWallet;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.CompteWalletRepository;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.UtilisateurRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Crée les comptes manquants : ceux d'un utilisateur (un par devise) et ceux du système
 * (le coffre de RucoMali et le « monde extérieur » simulé).
 * Les méthodes sont synchronisées et validées dans le verrou : deux requêtes simultanées ne créent pas de doublons.
 */
@Component
public class ComptesWalletInitialiseur {

    /** Liquidité de départ du coffre pour la démonstration (montants fictifs). */
    private static final Map<String, BigDecimal> LIQUIDITE_INITIALE = Map.of(
        "RUB", new BigDecimal("5250000"),
        "XOF", new BigDecimal("8500000"),
        "EUR", new BigDecimal("120000"),
        "USD", new BigDecimal("80000")
    );

    /** Liquidité visée pour une devise : sert à calculer le niveau du coffre (élevée, normale, faible, très faible). */
    public static BigDecimal cibleLiquidite(String codeDevise) {
        return LIQUIDITE_INITIALE.getOrDefault(codeDevise, BigDecimal.ZERO);
    }

    private final CompteWalletRepository comptes;
    private final DeviseRepository deviseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final GrandLivre grandLivre;
    private final TransactionTemplate transaction;

    private final Set<Long> utilisateursPrets = ConcurrentHashMap.newKeySet();
    private volatile boolean systemePret = false;

    public ComptesWalletInitialiseur(
        CompteWalletRepository comptes,
        DeviseRepository deviseRepository,
        UtilisateurRepository utilisateurRepository,
        GrandLivre grandLivre,
        PlatformTransactionManager gestionnaireTransactions
    ) {
        this.comptes = comptes;
        this.deviseRepository = deviseRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.grandLivre = grandLivre;
        this.transaction = new TransactionTemplate(gestionnaireTransactions);
    }

    public synchronized void assurerComptesUtilisateur(Long utilisateurId) {
        if (utilisateursPrets.contains(utilisateurId)) return;
        transaction.executeWithoutResult(statut -> {
            Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElseThrow();
            for (Devise devise : deviseRepository.findByActiveTrueOrderByCodeAsc()) {
                if (!comptes.existsByProprietaireIdAndDevise_CodeAndType(utilisateurId, devise.getCode(), CompteWallet.Type.UTILISATEUR)) {
                    comptes.save(new CompteWallet(utilisateur, devise, CompteWallet.Type.UTILISATEUR));
                }
            }
        });
        utilisateursPrets.add(utilisateurId);
    }

    public synchronized void assurerComptesSysteme() {
        if (systemePret) return;
        transaction.executeWithoutResult(statut -> {
            for (Devise devise : deviseRepository.findByActiveTrueOrderByCodeAsc()) {
                String code = devise.getCode();

                CompteWallet externe = comptes
                    .findByTypeAndProprietaireIsNullAndDevise_Code(CompteWallet.Type.EXTERNE, code)
                    .orElseGet(() -> comptes.save(new CompteWallet(null, devise, CompteWallet.Type.EXTERNE)));

                if (comptes.findByTypeAndProprietaireIsNullAndDevise_Code(CompteWallet.Type.COFFRE, code).isEmpty()) {
                    CompteWallet coffre = comptes.save(new CompteWallet(null, devise, CompteWallet.Type.COFFRE));
                    BigDecimal apport = LIQUIDITE_INITIALE.getOrDefault(code, BigDecimal.ZERO);
                    if (apport.signum() > 0) {
                        grandLivre.passer(OperationWallet.Type.APPORT_COFFRE, null, null, List.of(
                            new GrandLivre.Ligne(externe, EcritureWallet.Sens.DEBIT, apport),
                            new GrandLivre.Ligne(coffre, EcritureWallet.Sens.CREDIT, apport)
                        ));
                    }
                }
            }
        });
        systemePret = true;
    }
}
