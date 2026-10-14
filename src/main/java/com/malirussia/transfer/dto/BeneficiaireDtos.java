package com.malirussia.transfer.dto;

import com.malirussia.transfer.model.Beneficiaire;
import jakarta.validation.constraints.NotBlank;

public class BeneficiaireDtos {

    public static class BeneficiaireRequest {
        @NotBlank
        public String nomComplet;
        @NotBlank
        public String pays;                 // "MALI" ou "RUSSIE"
        @NotBlank
        public String moyenReception;       // "MAXIT" ou "BANQUE_RUSSE"
        @NotBlank
        public String identifiantReception; // numéro MaxIt, compte ou carte
    }

    public static class BeneficiaireResponse {
        public Long id;
        public String nomComplet;
        public String pays;
        public String moyenReception;
        public String identifiantReception;

        public static BeneficiaireResponse depuis(Beneficiaire b) {
            BeneficiaireResponse r = new BeneficiaireResponse();
            r.id = b.getId();
            r.nomComplet = b.getNomComplet();
            r.pays = b.getPays().name();
            r.moyenReception = b.getMoyenReception().name();
            r.identifiantReception = b.getIdentifiantReception();
            return r;
        }
    }
}
