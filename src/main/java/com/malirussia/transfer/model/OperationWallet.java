package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Une opération du grand livre : regroupe les écritures qui la composent. */
@Entity
@Table(name = "operations_wallet")
public class OperationWallet {

    public enum Type { DEPOT, RETRAIT, CONVERSION, APPORT_COFFRE, TRANSFERT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36, updatable = false)
    private String reference = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    /** Vide pour les opérations faites par le système (apport initial du coffre). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiateur_id")
    private Utilisateur initiateur;

    @Column(precision = 18, scale = 8)
    private BigDecimal tauxApplique;

    /** Commission gagnée par RucoMali sur une conversion (frais + marge), exprimée en USD. */
    @Column(precision = 20, scale = 4)
    private BigDecimal commissionUsd;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public OperationWallet() {}

    public OperationWallet(Type type, Utilisateur initiateur, BigDecimal tauxApplique) {
        this.type = type;
        this.initiateur = initiateur;
        this.tauxApplique = tauxApplique;
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public Type getType() { return type; }
    public Utilisateur getInitiateur() { return initiateur; }
    public BigDecimal getTauxApplique() { return tauxApplique; }
    public BigDecimal getCommissionUsd() { return commissionUsd; }
    public void setCommissionUsd(BigDecimal commissionUsd) { this.commissionUsd = commissionUsd; }

    public LocalDateTime getDateCreation() { return dateCreation; }
}
