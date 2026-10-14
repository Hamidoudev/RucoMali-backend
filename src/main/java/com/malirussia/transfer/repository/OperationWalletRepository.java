package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.OperationWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface OperationWalletRepository extends JpaRepository<OperationWallet, Long> {

    long countByTypeNot(OperationWallet.Type type);

    /** Somme des commissions en USD ; vide (null) tant qu'aucune conversion n'a eu lieu. */
    @Query("select sum(o.commissionUsd) from OperationWallet o")
    BigDecimal totalCommissionsUsd();
}
