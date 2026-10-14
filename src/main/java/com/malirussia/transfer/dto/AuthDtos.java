package com.malirussia.transfer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Les messages de validation sont des clés (erreur.xxx) que le frontend traduit en français, anglais et russe.
 * Tous les champs de l'inscription sont obligatoires.
 */
public class AuthDtos {

    public static class InscriptionRequest {
        @NotBlank(message = "erreur.prenom.requis")
        @Size(min = 2, max = 50, message = "erreur.prenom.invalide")
        @Pattern(regexp = "^[\\p{L}][\\p{L}\\s'’.-]*$", message = "erreur.prenom.invalide")
        public String prenom;

        @NotBlank(message = "erreur.nom.requis")
        @Size(min = 2, max = 50, message = "erreur.nom.invalide")
        @Pattern(regexp = "^[\\p{L}][\\p{L}\\s'’.-]*$", message = "erreur.nom.invalide")
        public String nom;

        @NotBlank(message = "erreur.username.requis")
        @Size(min = 3, max = 30, message = "erreur.username.longueur")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "erreur.username.caracteres")
        public String username;

        @NotBlank(message = "erreur.email.requis")
        @Email(message = "erreur.email.invalide")
        @Size(max = 254, message = "erreur.email.invalide")
        public String email;

        @NotBlank(message = "erreur.telephone.requis")
        @Size(max = 30, message = "erreur.telephone.invalide")
        @Pattern(regexp = "^\\+?[0-9\\s().-]+$", message = "erreur.telephone.invalide")
        public String telephone;

        @NotBlank(message = "erreur.pays.invalide")
        public String pays; // "MALI" ou "RUSSIE"

        // 8 caractères minimum, avec au moins une lettre et un chiffre. 72 : limite de BCrypt.
        @NotBlank(message = "erreur.password.requis")
        @Size(min = 8, max = 72, message = "erreur.password.longueur")
        @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).*$", message = "erreur.password.complexite")
        public String password;
    }

    public static class ConnexionRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    // Exposé en JSON comme { "token": "...", "username": "...", "prenom": "...", "nom": "...", "role": "CLIENT" }.
    public static class AuthResponse {
        public String token;
        public String username;
        public String prenom;
        public String nom;
        public String role; // CLIENT ou ADMIN

        public AuthResponse(String token, String username, String prenom, String nom, String role) {
            this.token = token;
            this.username = username;
            this.prenom = prenom;
            this.nom = nom;
            this.role = role;
        }
    }
}
