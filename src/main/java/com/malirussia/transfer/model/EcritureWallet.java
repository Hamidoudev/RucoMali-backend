package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Une ligne du grand livre : un mouvement sur un compte. Jamais modifiée ni supprimée.
 * CREDIT augmente le solde du compte, DEBIT le diminue.
 */
@Entity
@Table(name = "ecritures_wallet")
public class EcritureWallet {

    public enum Sens { DEBIT, CREDIT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_id", nullable = false, updatable = false)
    private OperationWallet operation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compte_id", nullable = false, updatable = false)
    private CompteWallet compte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Sens sens;

    @Column(nullable = false, precision = 20, scale = 4, updatable = false)
    private BigDecimal montant;

    /** Solde du compte juste après cette écriture. */
    @Column(nullable = false, precision = 20, scale = 4, updatable = false)
    private BigDecimal soldeApres;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public EcritureWallet() {}

    public EcritureWallet(OperationWallet operation, CompteWallet compte, Sens sens, BigDecimal montant, BigDecimal soldeApres) {
        this.operation = operation;
        this.compte = compte;
        this.sens = sens;
        this.montant = montant;
        this.soldeApres = soldeApres;
    }

    public Long getId() { return id; }
    public OperationWallet getOperation() { return operation; }
    public CompteWallet getCompte() { return compte; }
    public Sens getSens() { return sens; }
    public BigDecimal getMontant() { return montant; }
    public BigDecimal getSoldeApres() { return soldeApres; }
    public LocalDateTime getDateCreation() { return dateCreation; }
}
