package com.malirussia.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DevisDtos {

    /** Le détail d'un envoi pour un mode donné (MATCH ou COFFRE). */
    public static class DevisResponse {
        public String deviseSource;
        public String deviseCible;
        public String mode;
        public BigDecimal montantEnvoye;
        public BigDecimal frais;
        public BigDecimal montantRecu;
        public BigDecimal tauxReference;
        public BigDecimal tauxApplique;
        public LocalDateTime dateTaux;
        public LocalDateTime expireLe;
    }

    /** Les deux modes côte à côte, pour que le client choisisse : attendre un match ou passer par le coffre. */
    public static class ComparaisonDevis {
        public DevisResponse match;
        public DevisResponse coffre;
    }
}
