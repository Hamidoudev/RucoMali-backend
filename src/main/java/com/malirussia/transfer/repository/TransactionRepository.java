package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByMatchId(Long matchId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Transaction t WHERE t.match.demandeMaliVersRussie.auteur.id = :utilisateurId " +
        "OR t.match.demandeRussieVersMali.auteur.id = :utilisateurId " +
        "ORDER BY t.dateCreation DESC"
    )
    List<Transaction> findToutesPourUtilisateur(Long utilisateurId);
}
