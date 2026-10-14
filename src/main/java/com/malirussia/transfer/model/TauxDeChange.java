package com.malirussia.transfer.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un taux de change entre deux devises, à un instant donné.
 * On conserve l'historique : le taux courant est le plus récent de la paire.
 */
@Entity
@Table(name = "taux_de_change")
public class TauxDeChange {

    public enum Source { API_PUBLIQUE, MANUEL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "devise_source_code", nullable = false)
    private Devise deviseSource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "devise_cible_code", nullable = false)
    private Devise deviseCible;

    /** 1 unité de deviseSource = taux unités de deviseCible. */
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal taux;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source = Source.API_PUBLIQUE;

    @Column(nullable = false)
    private LocalDateTime dateMiseAJour = LocalDateTime.now();

    public TauxDeChange() {}

    public TauxDeChange(Devise deviseSource, Devise deviseCible, BigDecimal taux, Source source) {
        this.deviseSource = deviseSource;
        this.deviseCible = deviseCible;
        this.taux = taux;
        this.source = source;
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Devise getDeviseSource() { return deviseSource; }
    public void setDeviseSource(Devise deviseSource) { this.deviseSource = deviseSource; }

    public Devise getDeviseCible() { return deviseCible; }
    public void setDeviseCible(Devise deviseCible) { this.deviseCible = deviseCible; }

    public BigDecimal getTaux() { return taux; }
    public void setTaux(BigDecimal taux) { this.taux = taux; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
}
