package com.malirussia.transfer.dto;

import com.malirussia.transfer.model.DemandeDeTransfert;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DemandeDtos {

    // Ce que le frontend envoie pour créer une demande (voir CreerDemande.jsx).
    public static class DemandeRequest {
        @NotBlank
        public String sens; // "MALI_RU" ou "RU_MALI"
        @NotNull @DecimalMin(value = "0.01")
        public BigDecimal montantDeviseSource;
        @NotBlank
        public String deviseSource;
        @NotBlank
        public String deviseCible;
    }

    // Ce que l'API renvoie : on ne sérialise jamais l'entité Utilisateur
    // directement (elle contient le mot de passe haché), on projette
    // uniquement les champs utiles, à l'identique du serializer Django.
    public static class DemandeResponse {
        public Long id;
        public Long auteur;
        public String auteurUsername;
        public String sens;
        public BigDecimal montantDeviseSource;
        public String deviseSource;
        public String deviseCible;
        public BigDecimal tauxIndicatif;
        public BigDecimal montantDeviseCibleEstime;
        public String statut;
        public LocalDateTime dateCreation;

        public static DemandeResponse depuis(DemandeDeTransfert d) {
            DemandeResponse r = new DemandeResponse();
            r.id = d.getId();
            r.auteur = d.getAuteur().getId();
            r.auteurUsername = d.getAuteur().getUsername();
            r.sens = d.getSens().name();
            r.montantDeviseSource = d.getMontantDeviseSource();
            r.deviseSource = d.getDeviseSource();
            r.deviseCible = d.getDeviseCible();
            r.tauxIndicatif = d.getTauxIndicatif();
            r.montantDeviseCibleEstime = d.getMontantDeviseCibleEstime();
            r.statut = d.getStatut().name();
            r.dateCreation = d.getDateCreation();
            return r;
        }

        public static Object depuis(Object o) {

            return o;
        }
    }
}
