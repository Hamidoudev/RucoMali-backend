package com.malirussia.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StatistiquesDtos {

    public static class PointJour {
        public LocalDate date;
        public BigDecimal volume;
    }

    public static class StatistiquesResponse {
        /** Monnaie du pays de l'utilisateur : RUB pour la Russie, XOF pour le Mali. */
        public String deviseReference;
        public int decimalesReference;
        public int jours;
        public List<PointJour> serie;
        public BigDecimal volumeTotal;
        public BigDecimal volumePrecedent;
        public long nombreTransferts;
        public long nombreTransfertsPrecedent;
        public BigDecimal fraisPayes;
        public BigDecimal fraisPrecedents;
    }
}
