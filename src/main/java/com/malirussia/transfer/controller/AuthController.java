package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.AuthDtos.AuthResponse;
import com.malirussia.transfer.dto.AuthDtos.ConnexionRequest;
import com.malirussia.transfer.dto.AuthDtos.InscriptionRequest;
import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** POST /api/auth/register : crée un compte et renvoie directement un token. */
    @PostMapping("/register")
    public AuthResponse inscription(@Valid @RequestBody InscriptionRequest requete) {
        // BCrypt refuse les mots de passe de plus de 72 octets (les lettres cyrilliques en pèsent 2).
        if (requete.password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.password.longueur");
        }

        String telephone = requete.telephone.trim();
        long chiffres = telephone.chars().filter(Character::isDigit).count();
        if (chiffres < 8 || chiffres > 15) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.telephone.invalide");
        }

        if (utilisateurRepository.existsByUsernameIgnoreCase(requete.username)) {
            throw new ApiException(HttpStatus.CONFLICT, "erreur.username.existant");
        }

        String email = requete.email.trim().toLowerCase(Locale.ROOT);
        if (utilisateurRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "erreur.email.existant");
        }

        Utilisateur.Pays pays;
        try {
            pays = Utilisateur.Pays.valueOf(requete.pays.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.pays.invalide");
        }

        Utilisateur utilisateur = new Utilisateur(
                requete.username,
                email,
                passwordEncoder.encode(requete.password),
                pays,
                telephone
        );
        utilisateur.setPrenom(requete.prenom.trim());
        utilisateur.setNom(requete.nom.trim());

        try {
            utilisateurRepository.save(utilisateur);
        } catch (DataIntegrityViolationException e) {
            // Deux inscriptions simultanées avec le même nom d'utilisateur.
            throw new ApiException(HttpStatus.CONFLICT, "erreur.username.existant");
        }

        return reponse(utilisateur);
    }

    /** POST /api/auth/login : vérifie le mot de passe et renvoie un token. */
    @PostMapping("/login")
    public AuthResponse connexion(@Valid @RequestBody ConnexionRequest requete) {
        Utilisateur utilisateur = utilisateurRepository.findByUsername(requete.username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "erreur.identifiants.invalides"));

        if (!passwordEncoder.matches(requete.password, utilisateur.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "erreur.identifiants.invalides");
        }

        return reponse(utilisateur);
    }

    private AuthResponse reponse(Utilisateur u) {
        return new AuthResponse(jwtUtil.genererToken(u.getUsername()), u.getUsername(), u.getPrenom(), u.getNom(), u.getRole().name());
    }
}
