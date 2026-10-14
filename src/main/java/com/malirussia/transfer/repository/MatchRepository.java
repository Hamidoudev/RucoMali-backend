package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @org.springframework.data.jpa.repository.Query(
        "SELECT m FROM Match m WHERE m.demandeMaliVersRussie.auteur.id = :utilisateurId " +
        "OR m.demandeRussieVersMali.auteur.id = :utilisateurId " +
        "ORDER BY m.dateMatch DESC"
    )
    List<Match> findTousPourUtilisateur(Long utilisateurId);
}
