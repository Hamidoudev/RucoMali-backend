package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.CompteWallet;
import com.malirussia.transfer.model.EcritureWallet;
import com.malirussia.transfer.model.OperationWallet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EcritureWalletRepository extends JpaRepository<EcritureWallet, Long> {

    /** Les dernières écritures sur les comptes d'un utilisateur, de la plus récente à la plus ancienne. */
    @Query("select e from EcritureWallet e join e.compte c "
        + "where c.proprietaire.id = :utilisateurId and c.type = :type order by e.id desc")
    List<EcritureWallet> recentes(
        @Param("utilisateurId") Long utilisateurId,
        @Param("type") CompteWallet.Type type,
        Pageable pageable
    );

    /** Les dernières écritures sur les comptes des utilisateurs (toutes personnes confondues). */
    @Query("select e from EcritureWallet e join e.compte c where c.type = :type order by e.id desc")
    List<EcritureWallet> recentesUtilisateurs(@Param("type") CompteWallet.Type type, Pageable pageable);

    /**
     * Volume traité par devise : dépôts reçus, retraits effectués et montants convertis (côté source).
     * Chaque ligne du résultat est [code de la devise, total].
     */
    @Query("select c.devise.code, sum(e.montant) from EcritureWallet e join e.compte c join e.operation o "
        + "where c.type = :compte and ("
        + "(o.type = :depot and e.sens = :credit) or "
        + "(o.type = :retrait and e.sens = :debit) or "
        + "(o.type = :conversion and e.sens = :debit) or "
        + "(o.type = :transfert and e.sens = :debit)) "
        + "group by c.devise.code")
    List<Object[]> volumesParDevise(
        @Param("compte") CompteWallet.Type compte,
        @Param("depot") OperationWallet.Type depot,
        @Param("retrait") OperationWallet.Type retrait,
        @Param("conversion") OperationWallet.Type conversion,
        @Param("transfert") OperationWallet.Type transfert,
        @Param("credit") EcritureWallet.Sens credit,
        @Param("debit") EcritureWallet.Sens debit
    );
}
