package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.Beneficiaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, Long> {

    List<Beneficiaire> findByProprietaireIdOrderByNomCompletAsc(Long proprietaireId);

    Optional<Beneficiaire> findByIdAndProprietaireId(Long id, Long proprietaireId);
}
