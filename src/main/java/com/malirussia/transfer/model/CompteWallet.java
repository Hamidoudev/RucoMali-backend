package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un compte dans une devise. Trois sortes de comptes :
 *  - UTILISATEUR : le RucoWallet d'un utilisateur (un compte par devise) ;
 *  - COFFRE : la liquidité de RucoMali (un compte par devise, sans propriétaire) ;
 *  - EXTERNE : le « monde extérieur » simulé (banques, MaxIt) qui sert de contrepartie aux dépôts et retraits.
 */
@Entity
@Table(
    name = "comptes_wallet",
    uniqueConstraints = @UniqueConstraint(columnNames = {"proprietaire_id", "devise_code", "type"})
)
public class CompteWallet {

    public enum Type { UTILISATEUR, COFFRE, EXTERNE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Vide pour les comptes COFFRE et EXTERNE. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id")
    private Utilisateur proprietaire;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "devise_code", nullable = false)
    private Devise devise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal solde = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public CompteWallet() {}

    public CompteWallet(Utilisateur proprietaire, Devise devise, Type type) {
        this.proprietaire = proprietaire;
        this.devise = devise;
        this.type = type;
    }

    public Long getId() { return id; }
    public Utilisateur getProprietaire() { return proprietaire; }
    public Devise getDevise() { return devise; }
    public Type getType() { return type; }
    public BigDecimal getSolde() { return solde; }
    public void setSolde(BigDecimal solde) { this.solde = solde; }
    public LocalDateTime getDateCreation() { return dateCreation; }
}
