package com.malirussia.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransfertDtos {

    public static class TransfertRequest {
        @NotNull(message = "erreur.beneficiaire.introuvable")
        public Long beneficiaireId;

        @NotBlank(message = "erreur.devise.inconnue")
        public String deviseSource;

        @NotBlank(message = "erreur.devise.inconnue")
        public String deviseCible;

        @NotNull(message = "erreur.montant.invalide")
        public BigDecimal montant;

        @NotBlank(message = "erreur.pin.format")
        @Pattern(regexp = "^\\d{6}$", message = "erreur.pin.format")
        public String pin;
    }

    public static class TransfertResponse {
        public String reference;
        public String statut; // PAIEMENT_CONFIRME, ENVOYE, TERMINE
        public String beneficiaireNom;
        public String beneficiairePays;
        public String beneficiaireMoyen;
        public String beneficiaireIdentifiant;
        public String deviseSource;
        public int decimalesSource;
        public BigDecimal montantSource;
        public BigDecimal frais;
        public String deviseCible;
        public int decimalesCible;
        public BigDecimal montantCible;
        public BigDecimal tauxApplique;
        public LocalDateTime dateCreation;
        public LocalDateTime dateMiseAJour;
    }
}
