package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "demande_mali_vers_russie_id", nullable = false)
    private DemandeDeTransfert demandeMaliVersRussie;

    @OneToOne
    @JoinColumn(name = "demande_russie_vers_mali_id", nullable = false)
    private DemandeDeTransfert demandeRussieVersMali;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateMatch = LocalDateTime.now();

    private boolean confirmeParMali = false;
    private boolean confirmeParRussie = false;

    public Match() {}

    public Match(DemandeDeTransfert demandeMaliVersRussie, DemandeDeTransfert demandeRussieVersMali) {
        this.demandeMaliVersRussie = demandeMaliVersRussie;
        this.demandeRussieVersMali = demandeRussieVersMali;
    }

    /** Champ calculé, exposé en JSON comme est_complet. */
    public boolean getEstComplet() {
        return confirmeParMali && confirmeParRussie;
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DemandeDeTransfert getDemandeMaliVersRussie() { return demandeMaliVersRussie; }
    public void setDemandeMaliVersRussie(DemandeDeTransfert d) { this.demandeMaliVersRussie = d; }

    public DemandeDeTransfert getDemandeRussieVersMali() { return demandeRussieVersMali; }
    public void setDemandeRussieVersMali(DemandeDeTransfert d) { this.demandeRussieVersMali = d; }

    public LocalDateTime getDateMatch() { return dateMatch; }
    public void setDateMatch(LocalDateTime dateMatch) { this.dateMatch = dateMatch; }

    public boolean isConfirmeParMali() { return confirmeParMali; }
    public void setConfirmeParMali(boolean confirmeParMali) { this.confirmeParMali = confirmeParMali; }

    public boolean isConfirmeParRussie() { return confirmeParRussie; }
    public void setConfirmeParRussie(boolean confirmeParRussie) { this.confirmeParRussie = confirmeParRussie; }
}
