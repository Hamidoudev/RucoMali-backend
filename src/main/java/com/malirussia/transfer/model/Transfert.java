package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Un transfert d'argent vers un bénéficiaire. Les informations du bénéficiaire sont copiées au moment de l'envoi :
 * l'historique reste correct même si le bénéficiaire est modifié ou supprimé ensuite.
 */
@Entity
@Table(name = "transferts")
public class Transfert {

    /** Étapes de la démonstration : paiement confirmé, envoyé au partenaire, remis au bénéficiaire. */
    public enum Statut { PAIEMENT_CONFIRME, ENVOYE, TERMINE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36, updatable = false)
    private String reference = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private Utilisateur expediteur;

    private Long beneficiaireId;

    @Column(nullable = false)
    private String beneficiaireNom;

    @Column(nullable = false, length = 20)
    private String beneficiairePays;

    @Column(nullable = false, length = 30)
    private String beneficiaireMoyen;

    @Column(nullable = false)
    private String beneficiaireIdentifiant;

    @Column(nullable = false, length = 5)
    private String deviseSource;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal montantSource;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal frais;

    @Column(nullable = false, length = 5)
    private String deviseCible;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal montantCible;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal tauxApplique;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.PAIEMENT_CONFIRME;

    @Column(length = 36)
    private String operationReference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dateMiseAJour = LocalDateTime.now();

    public Long getId() { return id; }
    public String getReference() { return reference; }

    public Utilisateur getExpediteur() { return expediteur; }
    public void setExpediteur(Utilisateur expediteur) { this.expediteur = expediteur; }

    public Long getBeneficiaireId() { return beneficiaireId; }
    public void setBeneficiaireId(Long beneficiaireId) { this.beneficiaireId = beneficiaireId; }

    public String getBeneficiaireNom() { return beneficiaireNom; }
    public void setBeneficiaireNom(String beneficiaireNom) { this.beneficiaireNom = beneficiaireNom; }

    public String getBeneficiairePays() { return beneficiairePays; }
    public void setBeneficiairePays(String beneficiairePays) { this.beneficiairePays = beneficiairePays; }

    public String getBeneficiaireMoyen() { return beneficiaireMoyen; }
    public void setBeneficiaireMoyen(String beneficiaireMoyen) { this.beneficiaireMoyen = beneficiaireMoyen; }

    public String getBeneficiaireIdentifiant() { return beneficiaireIdentifiant; }
    public void setBeneficiaireIdentifiant(String beneficiaireIdentifiant) { this.beneficiaireIdentifiant = beneficiaireIdentifiant; }

    public String getDeviseSource() { return deviseSource; }
    public void setDeviseSource(String deviseSource) { this.deviseSource = deviseSource; }

    public BigDecimal getMontantSource() { return montantSource; }
    public void setMontantSource(BigDecimal montantSource) { this.montantSource = montantSource; }

    public BigDecimal getFrais() { return frais; }
    public void setFrais(BigDecimal frais) { this.frais = frais; }

    public String getDeviseCible() { return deviseCible; }
    public void setDeviseCible(String deviseCible) { this.deviseCible = deviseCible; }

    public BigDecimal getMontantCible() { return montantCible; }
    public void setMontantCible(BigDecimal montantCible) { this.montantCible = montantCible; }

    public BigDecimal getTauxApplique() { return tauxApplique; }
    public void setTauxApplique(BigDecimal tauxApplique) { this.tauxApplique = tauxApplique; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getOperationReference() { return operationReference; }
    public void setOperationReference(String operationReference) { this.operationReference = operationReference; }

    public LocalDateTime getDateCreation() { return dateCreation; }

    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
}
