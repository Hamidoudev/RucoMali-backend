package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Un destinataire enregistré par un utilisateur (favori). */
@Entity
@Table(name = "beneficiaires")
public class Beneficiaire {

    public enum MoyenReception { MAXIT, BANQUE_RUSSE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Utilisateur proprietaire;

    @Column(nullable = false)
    private String nomComplet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Utilisateur.Pays pays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoyenReception moyenReception;

    /** Numéro MaxIt ou numéro de compte / de carte selon le moyen de réception. */
    @Column(nullable = false)
    private String identifiantReception;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Beneficiaire() {}

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getProprietaire() { return proprietaire; }
    public void setProprietaire(Utilisateur proprietaire) { this.proprietaire = proprietaire; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public Utilisateur.Pays getPays() { return pays; }
    public void setPays(Utilisateur.Pays pays) { this.pays = pays; }

    public MoyenReception getMoyenReception() { return moyenReception; }
    public void setMoyenReception(MoyenReception moyenReception) { this.moyenReception = moyenReception; }

    public String getIdentifiantReception() { return identifiantReception; }
    public void setIdentifiantReception(String identifiantReception) { this.identifiantReception = identifiantReception; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
