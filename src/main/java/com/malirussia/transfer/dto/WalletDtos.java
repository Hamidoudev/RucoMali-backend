package com.malirussia.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class WalletDtos {

    public static class OperationRequest {
        @NotBlank(message = "erreur.devise.inconnue")
        public String devise;
        @NotNull(message = "erreur.montant.invalide")
        public BigDecimal montant;
    }

    public static class ConversionRequest {
        @NotBlank(message = "erreur.devise.inconnue")
        public String source;
        @NotBlank(message = "erreur.devise.inconnue")
        public String cible;
        @NotNull(message = "erreur.montant.invalide")
        public BigDecimal montant;
    }

    public static class CompteResponse {
        public String devise;
        public String nom;
        public String symbole;
        public int decimales;
        public BigDecimal solde;
    }

    public static class WalletResponse {
        public List<CompteResponse> comptes;
    }

    public static class MouvementResponse {
        public Long id;
        public String reference;
        public String type;          // DEPOT, RETRAIT, CONVERSION
        public String devise;
        public int decimales;
        public BigDecimal montant;   // positif = crédit, négatif = débit
        public BigDecimal soldeApres;
        public LocalDateTime date;
        public BigDecimal taux;      // seulement pour une conversion
    }
}
