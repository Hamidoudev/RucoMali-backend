package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.BeneficiaireDtos.BeneficiaireRequest;
import com.malirussia.transfer.dto.BeneficiaireDtos.BeneficiaireResponse;
import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.Beneficiaire;
import com.malirussia.transfer.model.Notification;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.BeneficiaireRepository;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaires")
public class BeneficiaireController {

    private final BeneficiaireRepository beneficiaireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notifications;

    public BeneficiaireController(
        BeneficiaireRepository beneficiaireRepository,
        UtilisateurRepository utilisateurRepository,
        NotificationService notifications
    ) {
        this.beneficiaireRepository = beneficiaireRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notifications = notifications;
    }

    private Utilisateur utilisateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    /** GET /api/beneficiaires : les bénéficiaires de l'utilisateur connecté. */
    @GetMapping
    public List<BeneficiaireResponse> lister() {
        Long id = utilisateurConnecte().getId();
        return beneficiaireRepository.findByProprietaireIdOrderByNomCompletAsc(id)
            .stream().map(BeneficiaireResponse::depuis).toList();
    }

    /** POST /api/beneficiaires : enregistre un nouveau bénéficiaire. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaireResponse creer(@Valid @RequestBody BeneficiaireRequest requete) {
        Beneficiaire b = new Beneficiaire();
        b.setProprietaire(utilisateurConnecte());
        b.setNomComplet(requete.nomComplet.trim());
        b.setPays(lirePays(requete.pays));
        b.setMoyenReception(lireMoyen(requete.moyenReception));
        b.setIdentifiantReception(requete.identifiantReception.trim());
        Beneficiaire enregistre = beneficiaireRepository.save(b);
        notifications.creer(b.getProprietaire().getId(), Notification.Type.BENEFICIAIRE_AJOUTE, null, enregistre.getNomComplet(), null, null, null, null);
        return BeneficiaireResponse.depuis(enregistre);
    }

    /** DELETE /api/beneficiaires/{id} : supprime un bénéficiaire (uniquement le sien). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        Beneficiaire b = beneficiaireRepository.findByIdAndProprietaireId(id, utilisateurConnecte().getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "erreur.beneficiaire.introuvable"));
        beneficiaireRepository.delete(b);
    }

    private Utilisateur.Pays lirePays(String valeur) {
        try {
            return Utilisateur.Pays.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.pays.invalide");
        }
    }

    private Beneficiaire.MoyenReception lireMoyen(String valeur) {
        try {
            return Beneficiaire.MoyenReception.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.moyen.reception.invalide");
        }
    }
}
