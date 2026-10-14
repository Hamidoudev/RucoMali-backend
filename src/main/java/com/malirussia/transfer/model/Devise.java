package com.malirussia.transfer.model;

import jakarta.persistence.*;

/** Une devise gérée par RucoMali (RUB, XOF, USD, EUR). Le code sert d'identifiant. */
@Entity
@Table(name = "devises")
public class Devise {

    @Id
    @Column(length = 5)
    private String code;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, length = 5)
    private String symbole;

    /** Nombre de décimales affichées (2 pour RUB/USD/EUR, 0 pour XOF). */
    @Column(nullable = false)
    private int decimales;

    @Column(nullable = false)
    private boolean active = true;

    public Devise() {}

    public Devise(String code, String nom, String symbole, int decimales) {
        this.code = code;
        this.nom = nom;
        this.symbole = symbole;
        this.decimales = decimales;
    }

    // --- Getters / Setters ---
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getSymbole() { return symbole; }
    public void setSymbole(String symbole) { this.symbole = symbole; }

    public int getDecimales() { return decimales; }
    public void setDecimales(int decimales) { this.decimales = decimales; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
