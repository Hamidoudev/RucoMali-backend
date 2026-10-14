package com.malirussia.transfer.service;

import com.malirussia.transfer.model.Notification;
import com.malirussia.transfer.model.Transfert;
import com.malirussia.transfer.repository.TransfertRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Simule la livraison au bénéficiaire : paiement confirmé, puis envoyé au partenaire, puis remis.
 * Dans une vraie application, ces changements d'état viendraient de MaxIt ou de la banque.
 */
@Component
public class TransfertAvancement {

    private final TransfertRepository transferts;
    private final NotificationService notifications;

    public TransfertAvancement(TransfertRepository transferts, NotificationService notifications) {
        this.transferts = transferts;
        this.notifications = notifications;
    }

    @Scheduled(fixedDelay = 4000)
    public void avancer() {
        LocalDateTime maintenant = LocalDateTime.now();

        for (Transfert t : transferts.findByStatutAndDateMiseAJourBefore(Transfert.Statut.PAIEMENT_CONFIRME, maintenant.minusSeconds(8))) {
            t.setStatut(Transfert.Statut.ENVOYE);
            t.setDateMiseAJour(maintenant);
            transferts.save(t);
        }
        for (Transfert t : transferts.findByStatutAndDateMiseAJourBefore(Transfert.Statut.ENVOYE, maintenant.minusSeconds(12))) {
            t.setStatut(Transfert.Statut.TERMINE);
            t.setDateMiseAJour(maintenant);
            transferts.save(t);
            notifications.creer(
                transferts.expediteurId(t.getId()), Notification.Type.TRANSFERT_TERMINE,
                t.getReference(), t.getBeneficiaireNom(), t.getMontantCible(), t.getDeviseCible(), null, null
            );
        }
    }
}
