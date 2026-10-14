package com.malirussia.transfer.service;

import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.CompteWallet;
import com.malirussia.transfer.model.EcritureWallet;
import com.malirussia.transfer.model.OperationWallet;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.CompteWalletRepository;
import com.malirussia.transfer.repository.EcritureWalletRepository;
import com.malirussia.transfer.repository.OperationWalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Le grand livre : toute modification d'un solde passe ici, sous forme d'écritures en partie double.
 * Pour chaque devise d'une opération, la somme des débits égale la somme des crédits.
 * À appeler dans une transaction, avec les comptes déjà verrouillés.
 */
@Component
public class GrandLivre {

    public record Ligne(CompteWallet compte, EcritureWallet.Sens sens, BigDecimal montant) {}

    private final OperationWalletRepository operations;
    private final EcritureWalletRepository ecritures;
    private final CompteWalletRepository comptes;

    public GrandLivre(
        OperationWalletRepository operations,
        EcritureWalletRepository ecritures,
        CompteWalletRepository comptes
    ) {
        this.operations = operations;
        this.ecritures = ecritures;
        this.comptes = comptes;
    }

    public OperationWallet passer(OperationWallet.Type type, Utilisateur initiateur, BigDecimal taux, List<Ligne> lignes) {
        verifierEquilibre(lignes);
        OperationWallet operation = operations.save(new OperationWallet(type, initiateur, taux));

        for (Ligne ligne : lignes) {
            CompteWallet compte = ligne.compte();
            BigDecimal nouveauSolde = ligne.sens() == EcritureWallet.Sens.CREDIT
                ? compte.getSolde().add(ligne.montant())
                : compte.getSolde().subtract(ligne.montant());

            // Seul le « monde extérieur » simulé peut être négatif.
            if (compte.getType() != CompteWallet.Type.EXTERNE && nouveauSolde.signum() < 0) {
                throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    compte.getType() == CompteWallet.Type.COFFRE ? "erreur.liquidite.insuffisante" : "erreur.solde.insuffisant"
                );
            }

            compte.setSolde(nouveauSolde);
            comptes.save(compte);
            ecritures.save(new EcritureWallet(operation, compte, ligne.sens(), ligne.montant(), nouveauSolde));
        }
        return operation;
    }

    private void verifierEquilibre(List<Ligne> lignes) {
        Map<String, BigDecimal> parDevise = new HashMap<>();
        for (Ligne l : lignes) {
            BigDecimal effet = l.sens() == EcritureWallet.Sens.CREDIT ? l.montant() : l.montant().negate();
            parDevise.merge(l.compte().getDevise().getCode(), effet, BigDecimal::add);
        }
        for (BigDecimal total : parDevise.values()) {
            if (total.signum() != 0) {
                throw new IllegalStateException("Opération déséquilibrée : les débits et les crédits diffèrent.");
            }
        }
    }
}
