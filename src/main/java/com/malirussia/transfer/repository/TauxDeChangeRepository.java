package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.TauxDeChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TauxDeChangeRepository extends JpaRepository<TauxDeChange, Long> {

    /** Le taux le plus récent d'une paire de devises. */
    Optional<TauxDeChange> findFirstByDeviseSource_CodeAndDeviseCible_CodeOrderByDateMiseAJourDesc(
        String codeSource, String codeCible
    );
}
