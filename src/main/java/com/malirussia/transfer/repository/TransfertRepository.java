package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.Transfert;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransfertRepository extends JpaRepository<Transfert, Long> {

    List<Transfert> findByExpediteurIdOrderByIdDesc(Long expediteurId, Pageable pageable);

    Optional<Transfert> findByReferenceAndExpediteurId(String reference, Long expediteurId);

    List<Transfert> findByStatutAndDateMiseAJourBefore(Transfert.Statut statut, LocalDateTime avant);

    List<Transfert> findByExpediteurIdAndDateCreationGreaterThanEqual(Long expediteurId, LocalDateTime depuis);

    /** L'identifiant de l'expéditeur, sans charger l'utilisateur. */
    @Query("select t.expediteur.id from Transfert t where t.id = :id")
    Long expediteurId(@Param("id") Long id);
}
