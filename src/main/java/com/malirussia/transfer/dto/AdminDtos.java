package com.malirussia.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDtos {

    public static class ApportRequest {
        @NotBlank(message = "erreur.devise.inconnue")
        public String devise;
        @NotNull(message = "erreur.montant.invalide")
        public BigDecimal montant;
    }

    public static class VolumeDevise {
        public String devise;
        public BigDecimal volume;     // dans la devise elle-même
        public BigDecimal volumeUsd;  // équivalent en dollars
    }

    public static class LiquiditeResponse {
        public String devise;
        public String nom;
        public String symbole;
        public int decimales;
        public BigDecimal solde;
        public BigDecimal cible;
        public String niveau; // ELEVEE, NORMALE, FAIBLE, TRES_FAIBLE
    }

    public static class LigneTransaction {
        public String devise;
        public int decimales;
        public BigDecimal montant; // négatif = débit
    }

    public static class TransactionAdminResponse {
        public String reference;
        public String type;
        public String utilisateur;
        public LocalDateTime date;
        public List<LigneTransaction> lignes = new ArrayList<>();
        public BigDecimal taux;
        public BigDecimal commissionUsd;
    }

    public static class ApercuResponse {
        public long utilisateurs;
        public long transactions;
        public BigDecimal volumeTotalUsd;
        public BigDecimal commissionsUsd;
        public int alertes;
        public List<VolumeDevise> volumes;
        public List<LiquiditeResponse> liquidite;
        public List<TransactionAdminResponse> recentes;
    }

    public static class UtilisateurAdminResponse {
        public Long id;
        public String username;
        public String prenom;
        public String nom;
        public String email;
        public String pays;
        public String telephone;
        public String role;
        public LocalDateTime dateCreation;
    }

    public static class TauxResponse {
        public String source;
        public String cible;
        public BigDecimal taux;
        public String origine; // API_PUBLIQUE ou MANUEL
        public LocalDateTime date;
    }
}
