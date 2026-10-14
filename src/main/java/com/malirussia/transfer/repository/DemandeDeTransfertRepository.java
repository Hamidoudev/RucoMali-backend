package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.DemandeDeTransfert;
import com.malirussia.transfer.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeDeTransfertRepository extends JpaRepository<DemandeDeTransfert, Long> {

    List<DemandeDeTransfert> findByAuteurOrderByDateCreationDesc(Utilisateur auteur);

    List<DemandeDeTransfert> findByStatutOrderByDateCreationDesc(DemandeDeTransfert.Statut statut);

    List<DemandeDeTransfert> findBySensAndStatut(DemandeDeTransfert.Sens sens, DemandeDeTransfert.Statut statut);
}