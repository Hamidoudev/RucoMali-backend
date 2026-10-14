package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Une notification pour un utilisateur. On conserve le type et ses valeurs (nom, montants) :
 * le texte est composé par l'interface, dans la langue de l'utilisateur.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    public enum Type { TRANSFERT_TERMINE, DEPOT, RETRAIT, CONVERSION, BENEFICIAIRE_AJOUTE, PIN_DEFINI }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Type type;

    /** Référence du transfert concerné, le cas échéant. */
    @Column(length = 36)
    private String reference;

    /** Nom du bénéficiaire, le cas échéant. */
    private String nom;

    @Column(precision = 20, scale = 4)
    private BigDecimal montant;

    @Column(length = 5)
    private String devise;

    /** Second montant (par exemple le montant obtenu après une conversion). */
    @Column(precision = 20, scale = 4)
    private BigDecimal montant2;

    @Column(length = 5)
    private String devise2;

    @Column(nullable = false)
    private boolean lue = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Notification() {}

    public Notification(Utilisateur utilisateur, Type type) {
        this.utilisateur = utilisateur;
        this.type = type;
    }

    public Long getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public Type getType() { return type; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public BigDecimal getMontant2() { return montant2; }
    public void setMontant2(BigDecimal montant2) { this.montant2 = montant2; }

    public String getDevise2() { return devise2; }
    public void setDevise2(String devise2) { this.devise2 = devise2; }

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public LocalDateTime getDateCreation() { return dateCreation; }
}
