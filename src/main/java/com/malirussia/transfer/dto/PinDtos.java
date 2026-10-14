package com.malirussia.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PinDtos {

    public static class PinRequest {
        @NotBlank(message = "erreur.pin.format")
        @Pattern(regexp = "^\\d{6}$", message = "erreur.pin.format")
        public String pin;
    }

    public static class ChangementPinRequest {
        @NotBlank(message = "erreur.pin.format")
        @Pattern(regexp = "^\\d{6}$", message = "erreur.pin.format")
        public String ancienPin;

        @NotBlank(message = "erreur.pin.format")
        @Pattern(regexp = "^\\d{6}$", message = "erreur.pin.format")
        public String nouveauPin;
    }

    public static class EtatPinResponse {
        public boolean defini;
        public boolean bloque;
    }
}
