package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.CompteWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompteWalletRepository extends JpaRepository<CompteWallet, Long> {

    List<CompteWallet> findByProprietaireIdAndType(Long proprietaireId, CompteWallet.Type type);

    Optional<CompteWallet> findByProprietaireIdAndDevise_CodeAndType(Long proprietaireId, String codeDevise, CompteWallet.Type type);

    boolean existsByProprietaireIdAndDevise_CodeAndType(Long proprietaireId, String codeDevise, CompteWallet.Type type);

    /** Comptes sans propriétaire (COFFRE, EXTERNE). */
    Optional<CompteWallet> findByTypeAndProprietaireIsNullAndDevise_Code(CompteWallet.Type type, String codeDevise);

    List<CompteWallet> findByTypeAndProprietaireIsNull(CompteWallet.Type type);

    /** Verrouille les comptes pour la durée de la transaction, dans l'ordre des identifiants (évite les blocages croisés). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CompteWallet c where c.id in :ids order by c.id")
    List<CompteWallet> verrouiller(@Param("ids") Collection<Long> ids);
}
