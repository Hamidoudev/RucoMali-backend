package com.malirussia.transfer.service;

import com.malirussia.transfer.dto.NotificationDtos.NotificationResponse;
import com.malirussia.transfer.dto.NotificationDtos.NotificationsResponse;
import com.malirussia.transfer.model.Notification;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.NotificationRepository;
import com.malirussia.transfer.repository.UtilisateurRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final UtilisateurRepository utilisateurs;
    private final DeviseRepository devises;

    public NotificationService(NotificationRepository notifications, UtilisateurRepository utilisateurs, DeviseRepository devises) {
        this.notifications = notifications;
        this.utilisateurs = utilisateurs;
        this.devises = devises;
    }

    /** Crée une notification. Les valeurs inutiles pour ce type de notification restent vides (null). */
    public void creer(
        Long utilisateurId, Notification.Type type, String reference, String nom,
        BigDecimal montant, String devise, BigDecimal montant2, String devise2
    ) {
        Notification n = new Notification(utilisateurs.getReferenceById(utilisateurId), type);
        n.setReference(reference);
        n.setNom(nom);
        n.setMontant(montant);
        n.setDevise(devise);
        n.setMontant2(montant2);
        n.setDevise2(devise2);
        notifications.save(n);
    }

    @Transactional(readOnly = true)
    public NotificationsResponse lister(Long utilisateurId, int limite) {
        NotificationsResponse reponse = new NotificationsResponse();
        reponse.nonLues = notifications.countByUtilisateurIdAndLueFalse(utilisateurId);
        reponse.notifications = new ArrayList<>();
        for (Notification n : notifications.findByUtilisateurIdOrderByIdDesc(utilisateurId, PageRequest.of(0, limite))) {
            NotificationResponse r = new NotificationResponse();
            r.id = n.getId();
            r.type = n.getType().name();
            r.reference = n.getReference();
            r.nom = n.getNom();
            r.montant = n.getMontant();
            r.devise = n.getDevise();
            r.decimales = decimales(n.getDevise());
            r.montant2 = n.getMontant2();
            r.devise2 = n.getDevise2();
            r.decimales2 = decimales(n.getDevise2());
            r.lue = n.isLue();
            r.date = n.getDateCreation();
            reponse.notifications.add(r);
        }
        return reponse;
    }

    @Transactional
    public void toutMarquerLu(Long utilisateurId) {
        notifications.marquerToutesLues(utilisateurId);
    }

    private int decimales(String code) {
        if (code == null) return 2;
        return devises.findById(code).map(d -> d.getDecimales()).orElse(2);
    }
}
