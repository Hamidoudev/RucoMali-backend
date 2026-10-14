package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurIdOrderByIdDesc(Long utilisateurId, Pageable pageable);

    long countByUtilisateurIdAndLueFalse(Long utilisateurId);

    @Modifying
    @Query("update Notification n set n.lue = true where n.utilisateur.id = :utilisateurId and n.lue = false")
    int marquerToutesLues(@Param("utilisateurId") Long utilisateurId);
}
