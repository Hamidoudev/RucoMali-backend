package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    public enum Pays { MALI, RUSSIE }

    /** CLIENT : espace client. ADMIN : espace administrateur. */
    public enum Role { CLIENT, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // Prénom et nom : obligatoires à l'inscription (contrôlé par l'API). Les colonnes restent
    // facultatives en base pour ne pas bloquer les comptes créés avant leur ajout.
    @Column(length = 50)
    private String prenom;

    @Column(length = 50)
    private String nom;

    private String email;

    @Column(nullable = false)
    private String password; // haché avec BCrypt, jamais stocké en clair

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pays pays;

    private String telephone;

    private String moyenPaiementLocal;

    private Integer reputationScore = 100;

    // Colonnes facultatives en base : les comptes créés avant leur ajout restent valides (rôle CLIENT par défaut).
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role = Role.CLIENT;

    @Column(updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    // Code PIN à 6 chiffres qui confirme les transferts : seul son hachage BCrypt est conservé.
    @Column(length = 100)
    private String pinHash;

    private Integer pinEchecs = 0;

    private LocalDateTime pinBloqueJusqua;

    public Utilisateur() {}

    public Utilisateur(String username, String email, String password, Pays pays, String telephone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.pays = pays;
        this.telephone = telephone;
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Pays getPays() { return pays; }
    public void setPays(Pays pays) { this.pays = pays; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getMoyenPaiementLocal() { return moyenPaiementLocal; }
    public void setMoyenPaiementLocal(String moyenPaiementLocal) { this.moyenPaiementLocal = moyenPaiementLocal; }

    public Role getRole() { return role == null ? Role.CLIENT : role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public boolean isPinDefini() { return pinHash != null && !pinHash.isBlank(); }

    public int getPinEchecs() { return pinEchecs == null ? 0 : pinEchecs; }
    public void setPinEchecs(int pinEchecs) { this.pinEchecs = pinEchecs; }

    public LocalDateTime getPinBloqueJusqua() { return pinBloqueJusqua; }
    public void setPinBloqueJusqua(LocalDateTime pinBloqueJusqua) { this.pinBloqueJusqua = pinBloqueJusqua; }

    public Integer getReputationScore() { return reputationScore; }
    public void setReputationScore(Integer reputationScore) { this.reputationScore = reputationScore; }
}
