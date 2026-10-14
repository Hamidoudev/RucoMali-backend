package com.malirussia.transfer.config;

import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée le compte administrateur au démarrage, d'après application.properties :
 *   app.admin.username=admin
 *   app.admin.password=un-mot-de-passe-solide
 * Si le compte existe déjà, il est simplement promu administrateur (son mot de passe n'est pas modifié).
 * Sans ces deux propriétés, rien n'est créé.
 */
@Component
public class AdminInitialiseur implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitialiseur.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminInitialiseur(
        UtilisateurRepository utilisateurRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.username:}") String username,
        @Value("${app.admin.password:}") String password
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username.trim();
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (username.isEmpty()) return;

        Utilisateur existant = utilisateurRepository.findByUsername(username).orElse(null);
        if (existant != null) {
            if (existant.getRole() != Utilisateur.Role.ADMIN) {
                existant.setRole(Utilisateur.Role.ADMIN);
                utilisateurRepository.save(existant);
                log.info("Le compte « {} » est maintenant administrateur.", username);
            }
            return;
        }

        if (password.length() < 8) {
            log.warn("Compte administrateur non créé : app.admin.password doit contenir au moins 8 caractères.");
            return;
        }

        Utilisateur admin = new Utilisateur(
            username, username + "@rucomali.local", passwordEncoder.encode(password), Utilisateur.Pays.RUSSIE, "+70000000000"
        );
        admin.setPrenom("Admin");
        admin.setNom("RucoMali");
        admin.setRole(Utilisateur.Role.ADMIN);
        utilisateurRepository.save(admin);
        log.info("Compte administrateur « {} » créé.", username);
    }
}
