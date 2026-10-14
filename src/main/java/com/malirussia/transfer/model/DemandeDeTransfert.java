package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Une intention d'envoi d'argent, en attente d'un match.
 * Idée centrale du projet : personne n'envoie jamais d'argent à travers la
 * frontière. Le moteur de matching (voir MatchingService) associe deux
 * demandes de sens opposé et de montant compatible : chacun paie et reçoit
 * localement, dans son propre pays.
 */
@Entity
@Table(name = "demandes_de_transfert")
public class DemandeDeTransfert {

    public enum Sens { MALI_RU, RU_MALI }

    public enum Statut { EN_ATTENTE, MATCHEE, COMPLETE, ANNULEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sens sens;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantDeviseSource;

    @Column(nullable = false, length = 5)
    private String deviseSource;

    @Column(nullable = false, length = 5)
    private String deviseCible;

    @Column(precision = 12, scale = 6)
    private BigDecimal tauxIndicatif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.EN_ATTENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public DemandeDeTransfert() {}

    /** Champ calculé, exposé en JSON comme montant_devise_cible_estime. */
    public BigDecimal getMontantDeviseCibleEstime() {
        if (tauxIndicatif == null || montantDeviseSource == null) return null;
        return montantDeviseSource.multiply(tauxIndicatif).setScale(2, RoundingMode.HALF_UP);
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getAuteur() { return auteur; }
    public void setAuteur(Utilisateur auteur) { this.auteur = auteur; }

    public Sens getSens() { return sens; }
    public void setSens(Sens sens) { this.sens = sens; }

    public BigDecimal getMontantDeviseSource() { return montantDeviseSource; }
    public void setMontantDeviseSource(BigDecimal montantDeviseSource) { this.montantDeviseSource = montantDeviseSource; }

    public String getDeviseSource() { return deviseSource; }
    public void setDeviseSource(String deviseSource) { this.deviseSource = deviseSource; }

    public String getDeviseCible() { return deviseCible; }
    public void setDeviseCible(String deviseCible) { this.deviseCible = deviseCible; }

    public BigDecimal getTauxIndicatif() { return tauxIndicatif; }
    public void setTauxIndicatif(BigDecimal tauxIndicatif) { this.tauxIndicatif = tauxIndicatif; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
