package com.malirussia.transfer.service;

import com.malirussia.transfer.dto.AdminDtos.*;
import com.malirussia.transfer.model.CompteWallet;
import com.malirussia.transfer.model.Devise;
import com.malirussia.transfer.model.EcritureWallet;
import com.malirussia.transfer.model.OperationWallet;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.CompteWalletRepository;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.EcritureWalletRepository;
import com.malirussia.transfer.repository.OperationWalletRepository;
import com.malirussia.transfer.repository.TauxDeChangeRepository;
import com.malirussia.transfer.repository.UtilisateurRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Les chiffres de l'espace administrateur : aperçu, liquidité du coffre, transactions, utilisateurs, taux. */
@Service
public class AdminService {

    private static final List<String> ORDRE = List.of("RUB", "XOF", "EUR", "USD");

    private final UtilisateurRepository utilisateurs;
    private final CompteWalletRepository comptes;
    private final OperationWalletRepository operations;
    private final EcritureWalletRepository ecritures;
    private final DeviseRepository devises;
    private final TauxDeChangeRepository taux;
    private final ComptesWalletInitialiseur initialiseur;
    private final TauxPubliqueService tauxPublic;

    public AdminService(
        UtilisateurRepository utilisateurs,
        CompteWalletRepository comptes,
        OperationWalletRepository operations,
        EcritureWalletRepository ecritures,
        DeviseRepository devises,
        TauxDeChangeRepository taux,
        ComptesWalletInitialiseur initialiseur,
        TauxPubliqueService tauxPublic
    ) {
        this.utilisateurs = utilisateurs;
        this.comptes = comptes;
        this.operations = operations;
        this.ecritures = ecritures;
        this.devises = devises;
        this.taux = taux;
        this.initialiseur = initialiseur;
        this.tauxPublic = tauxPublic;
    }

    // ---------- Aperçu ----------

    @Transactional(readOnly = true)
    public ApercuResponse apercu() {
        ApercuResponse r = new ApercuResponse();
        r.utilisateurs = utilisateurs.count();
        r.transactions = operations.countByTypeNot(OperationWallet.Type.APPORT_COFFRE);
        BigDecimal commissions = operations.totalCommissionsUsd();
        r.commissionsUsd = commissions == null ? BigDecimal.ZERO : commissions.setScale(2, RoundingMode.HALF_UP);

        r.volumes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] ligne : ecritures.volumesParDevise(
            CompteWallet.Type.UTILISATEUR,
            OperationWallet.Type.DEPOT, OperationWallet.Type.RETRAIT, OperationWallet.Type.CONVERSION, OperationWallet.Type.TRANSFERT,
            EcritureWallet.Sens.CREDIT, EcritureWallet.Sens.DEBIT
        )) {
            VolumeDevise v = new VolumeDevise();
            v.devise = (String) ligne[0];
            v.volume = (BigDecimal) ligne[1];
            v.volumeUsd = enUsd(v.devise, v.volume).setScale(2, RoundingMode.HALF_UP);
            total = total.add(v.volumeUsd);
            r.volumes.add(v);
        }
        r.volumes.sort(Comparator.comparingInt((VolumeDevise v) -> rang(v.devise)).thenComparing(v -> v.devise));
        r.volumeTotalUsd = total;

        r.liquidite = liquidite();
        r.alertes = (int) r.liquidite.stream().filter(l -> "FAIBLE".equals(l.niveau) || "TRES_FAIBLE".equals(l.niveau)).count();
        r.recentes = transactions(5);
        return r;
    }

    // ---------- Liquidité du coffre ----------

    @Transactional(readOnly = true)
    public List<LiquiditeResponse> liquidite() {
        initialiseur.assurerComptesSysteme();
        List<CompteWallet> liste = new ArrayList<>(comptes.findByTypeAndProprietaireIsNull(CompteWallet.Type.COFFRE));
        liste.removeIf(c -> !c.getDevise().isActive());
        liste.sort(Comparator.comparingInt((CompteWallet c) -> rang(c.getDevise().getCode())).thenComparing(c -> c.getDevise().getCode()));

        List<LiquiditeResponse> resultat = new ArrayList<>();
        for (CompteWallet c : liste) {
            LiquiditeResponse l = new LiquiditeResponse();
            l.devise = c.getDevise().getCode();
            l.nom = c.getDevise().getNom();
            l.symbole = c.getDevise().getSymbole();
            l.decimales = c.getDevise().getDecimales();
            l.solde = c.getSolde();
            l.cible = ComptesWalletInitialiseur.cibleLiquidite(l.devise);
            l.niveau = niveau(l.solde, l.cible);
            resultat.add(l);
        }
        return resultat;
    }

    /** Niveau du coffre par rapport à la liquidité visée : 75 % et plus élevé, 40 % normal, 15 % faible, en dessous très faible. */
    private String niveau(BigDecimal solde, BigDecimal cible) {
        if (cible.signum() <= 0) return "NORMALE";
        double ratio = solde.divide(cible, 4, RoundingMode.HALF_UP).doubleValue();
        if (ratio >= 0.75) return "ELEVEE";
        if (ratio >= 0.40) return "NORMALE";
        if (ratio >= 0.15) return "FAIBLE";
        return "TRES_FAIBLE";
    }

    // ---------- Transactions ----------

    @Transactional(readOnly = true)
    public List<TransactionAdminResponse> transactions(int limite) {
        int taillePage = limite * 2;
        List<EcritureWallet> lignes = ecritures.recentesUtilisateurs(CompteWallet.Type.UTILISATEUR, PageRequest.of(0, taillePage));

        Map<Long, TransactionAdminResponse> parOperation = new LinkedHashMap<>();
        for (EcritureWallet e : lignes) {
            OperationWallet o = e.getOperation();
            TransactionAdminResponse t = parOperation.computeIfAbsent(o.getId(), id -> {
                TransactionAdminResponse nouveau = new TransactionAdminResponse();
                nouveau.reference = o.getReference();
                nouveau.type = o.getType().name();
                nouveau.utilisateur = o.getInitiateur() == null ? null : o.getInitiateur().getUsername();
                nouveau.date = o.getDateCreation();
                nouveau.taux = o.getTauxApplique();
                nouveau.commissionUsd = o.getCommissionUsd();
                return nouveau;
            });
            LigneTransaction l = new LigneTransaction();
            l.devise = e.getCompte().getDevise().getCode();
            l.decimales = e.getCompte().getDevise().getDecimales();
            l.montant = e.getSens() == EcritureWallet.Sens.CREDIT ? e.getMontant() : e.getMontant().negate();
            t.lignes.add(l);
        }

        List<TransactionAdminResponse> resultat = new ArrayList<>(parOperation.values());
        // La dernière conversion peut être coupée par la taille de la page : on ne l'affiche pas à moitié.
        if (lignes.size() == taillePage && !resultat.isEmpty()) {
            TransactionAdminResponse derniere = resultat.get(resultat.size() - 1);
            if ("CONVERSION".equals(derniere.type) && derniere.lignes.size() < 2) resultat.remove(resultat.size() - 1);
        }
        for (TransactionAdminResponse t : resultat) {
            t.lignes.sort(Comparator.comparingInt((LigneTransaction l) -> l.montant.signum()));
        }
        return resultat.size() > limite ? resultat.subList(0, limite) : resultat;
    }

    // ---------- Utilisateurs ----------

    @Transactional(readOnly = true)
    public List<UtilisateurAdminResponse> listerUtilisateurs() {
        List<UtilisateurAdminResponse> resultat = new ArrayList<>();
        for (Utilisateur u : utilisateurs.findAll(PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "id"))).getContent()) {
            UtilisateurAdminResponse r = new UtilisateurAdminResponse();
            r.id = u.getId();
            r.username = u.getUsername();
            r.prenom = u.getPrenom();
            r.nom = u.getNom();
            r.email = u.getEmail();
            r.pays = u.getPays() == null ? null : u.getPays().name();
            r.telephone = u.getTelephone();
            r.role = u.getRole().name();
            r.dateCreation = u.getDateCreation();
            resultat.add(r);
        }
        return resultat;
    }

    // ---------- Taux ----------

    @Transactional(readOnly = true)
    public List<TauxResponse> listerTaux() {
        List<Devise> actives = devises.findByActiveTrueOrderByCodeAsc();
        List<TauxResponse> resultat = new ArrayList<>();
        for (Devise source : actives) {
            for (Devise cible : actives) {
                if (source.getCode().equals(cible.getCode())) continue;
                taux.findFirstByDeviseSource_CodeAndDeviseCible_CodeOrderByDateMiseAJourDesc(source.getCode(), cible.getCode())
                    .ifPresent(t -> {
                        TauxResponse r = new TauxResponse();
                        r.source = source.getCode();
                        r.cible = cible.getCode();
                        r.taux = t.getTaux();
                        r.origine = t.getSource().name();
                        r.date = t.getDateMiseAJour();
                        resultat.add(r);
                    });
            }
        }
        return resultat;
    }

    /** Relance tout de suite la récupération des taux auprès de l'API publique. */
    public List<TauxResponse> rafraichirTaux() {
        tauxPublic.rafraichir();
        return listerTaux();
    }

    // ---------- Outils ----------

    private BigDecimal enUsd(String codeDevise, BigDecimal montant) {
        if ("USD".equals(codeDevise)) return montant;
        return taux.findFirstByDeviseSource_CodeAndDeviseCible_CodeOrderByDateMiseAJourDesc(codeDevise, "USD")
            .map(t -> montant.multiply(t.getTaux()))
            .orElse(BigDecimal.ZERO);
    }

    private int rang(String code) {
        int i = ORDRE.indexOf(code);
        return i < 0 ? ORDRE.size() : i;
    }
}
