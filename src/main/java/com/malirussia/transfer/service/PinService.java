package com.malirussia.transfer.service;

import com.malirussia.transfer.dto.PinDtos.EtatPinResponse;
import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.Notification;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Le code PIN à 6 chiffres qui confirme les transferts.
 * Il est haché comme un mot de passe. Après 5 erreurs de suite, il est bloqué pendant 15 minutes.
 * Les essais ratés sont enregistrés dans leur propre transaction : ils ne sont jamais annulés.
 */
@Service
public class PinService {

    private static final int MAX_ECHECS = 5;
    private static final Duration DUREE_BLOCAGE = Duration.ofMinutes(15);

    private enum Resultat { OK, NON_DEFINI, BLOQUE, INCORRECT }

    @PersistenceContext
    private EntityManager entityManager;

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transaction;
    private final NotificationService notifications;

    public PinService(
        UtilisateurRepository utilisateurRepository,
        PasswordEncoder passwordEncoder,
        PlatformTransactionManager gestionnaireTransactions,
        NotificationService notifications
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.notifications = notifications;
        this.transaction = new TransactionTemplate(gestionnaireTransactions);
    }

    public EtatPinResponse etat(Long utilisateurId) {
        Utilisateur u = utilisateurRepository.findById(utilisateurId).orElseThrow();
        EtatPinResponse r = new EtatPinResponse();
        r.defini = u.isPinDefini();
        r.bloque = u.getPinBloqueJusqua() != null && u.getPinBloqueJusqua().isAfter(LocalDateTime.now());
        return r;
    }

    /** Crée le code PIN (une seule fois). */
    public void definir(Long utilisateurId, String pin) {
        controlerForce(pin);
        transaction.executeWithoutResult(statut -> {
            Utilisateur u = verrouiller(utilisateurId);
            if (u.isPinDefini()) {
                throw new ApiException(HttpStatus.CONFLICT, "erreur.pin.deja.defini");
            }
            enregistrer(u, pin);
            notifications.creer(utilisateurId, Notification.Type.PIN_DEFINI, null, null, null, null, null, null);
        });
    }

    /** Change le code PIN : l'ancien doit être connu. */
    public void changer(Long utilisateurId, String ancienPin, String nouveauPin) {
        controlerForce(nouveauPin);
        verifier(utilisateurId, ancienPin);
        transaction.executeWithoutResult(statut -> enregistrer(verrouiller(utilisateurId), nouveauPin));
    }

    /** Vérifie le code PIN ; lève une erreur s'il est absent, bloqué ou incorrect. */
    public void verifier(Long utilisateurId, String pin) {
        Resultat resultat = transaction.execute(statut -> {
            Utilisateur u = verrouiller(utilisateurId);
            if (!u.isPinDefini()) return Resultat.NON_DEFINI;

            LocalDateTime maintenant = LocalDateTime.now();
            if (u.getPinBloqueJusqua() != null && u.getPinBloqueJusqua().isAfter(maintenant)) return Resultat.BLOQUE;

            if (passwordEncoder.matches(pin, u.getPinHash())) {
                if (u.getPinEchecs() != 0 || u.getPinBloqueJusqua() != null) {
                    u.setPinEchecs(0);
                    u.setPinBloqueJusqua(null);
                    utilisateurRepository.save(u);
                }
                return Resultat.OK;
            }

            int echecs = u.getPinEchecs() + 1;
            if (echecs >= MAX_ECHECS) {
                u.setPinEchecs(0);
                u.setPinBloqueJusqua(maintenant.plus(DUREE_BLOCAGE));
                utilisateurRepository.save(u);
                return Resultat.BLOQUE;
            }
            u.setPinEchecs(echecs);
            utilisateurRepository.save(u);
            return Resultat.INCORRECT;
        });

        switch (resultat) {
            case OK -> { }
            case NON_DEFINI -> throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.pin.non.defini");
            // 423 et non 401 : un PIN bloqué ne doit pas fermer la session.
            case BLOQUE -> throw new ApiException(HttpStatus.LOCKED, "erreur.pin.bloque");
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.pin.incorrect");
        }
    }

    // ---------- Outils ----------

    /** Charge l'utilisateur en verrouillant sa ligne : deux essais simultanés ne contournent pas le compteur. */
    private Utilisateur verrouiller(Long utilisateurId) {
        Utilisateur u = entityManager.find(Utilisateur.class, utilisateurId, LockModeType.PESSIMISTIC_WRITE);
        if (u == null) throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.pin.non.defini");
        return u;
    }

    private void enregistrer(Utilisateur u, String pin) {
        u.setPinHash(passwordEncoder.encode(pin));
        u.setPinEchecs(0);
        u.setPinBloqueJusqua(null);
        utilisateurRepository.save(u);
    }

    /** 6 chiffres, ni répétés (111111), ni en suite (123456, 654321). */
    private void controlerForce(String pin) {
        if (pin == null || !pin.matches("^\\d{6}$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.pin.format");
        }
        boolean repete = pin.chars().distinct().count() == 1;
        boolean suite = "0123456789".contains(pin) || "9876543210".contains(pin);
        if (repete || suite) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.pin.faible");
        }
    }
}
