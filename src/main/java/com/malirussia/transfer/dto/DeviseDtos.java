package com.malirussia.transfer.dto;

import com.malirussia.transfer.model.Devise;

public class DeviseDtos {

    public static class DeviseResponse {
        public String code;
        public String nom;
        public String symbole;
        public int decimales;

        public static DeviseResponse depuis(Devise d) {
            DeviseResponse r = new DeviseResponse();
            r.code = d.getCode();
            r.nom = d.getNom();
            r.symbole = d.getSymbole();
            r.decimales = d.getDecimales();
            return r;
        }
    }
}
