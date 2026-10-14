package com.malirussia.transfer.service;

import com.malirussia.transfer.dto.DevisDtos.DevisResponse;
import com.malirussia.transfer.dto.WalletDtos.CompteResponse;
import com.malirussia.transfer.dto.WalletDtos.MouvementResponse;
import com.malirussia.transfer.dto.WalletDtos.WalletResponse;
import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.Beneficiaire;
import com.malirussia.transfer.model.CompteWallet;
import com.malirussia.transfer.model.Devise;
import com.malirussia.transfer.model.EcritureWallet;
import com.malirussia.transfer.model.Notification;
import com.malirussia.transfer.model.OperationWallet;
import com.malirussia.transfer.model.Transfert;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.CompteWalletRepository;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.EcritureWalletRepository;
import com.malirussia.transfer.repository.TauxDeChangeRepository;
import com.malirussia.transfer.repository.TransfertRepository;
import com.malirussia.transfer.service.GrandLivre.Ligne;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Le RucoWallet d'un utilisateur : soldes par devise, dépôt, retrait et conversion.
 * Dans la démonstration, dépôts et retraits sont simulés : la contrepartie est le compte EXTERNE.
 * Les conversions s'effectuent avec le coffre de RucoMali, au taux du mode « coffre » (marge comprise).
 */
@Service
public class WalletService {

    private static final List<String> ORDRE = List.of("RUB", "XOF", "EUR", "USD");
    private static final BigDecimal MONTANT_MAX_APPORT = new BigDecimal("1000000000");

    private final CompteWalletRepository comptes;
    private final EcritureWalletRepository ecritures;
    private final DeviseRepository deviseRepository;
    private final TauxDeChangeRepository tauxRepository;
    private final TransfertRepository transferts;
    private final DevisService devisService;
    private final GrandLivre grandLivre;
    private final NotificationService notifications;
    private final ComptesWalletInitialiseur initialiseur;
    private final TransactionTemplate transaction;
    private final BigDecimal montantMax;

    public WalletService(
        CompteWalletRepository comptes,
        EcritureWalletRepository ecritures,
        DeviseRepository deviseRepository,
        TauxDeChangeRepository tauxRepository,
        TransfertRepository transferts,
        DevisService devisService,
        GrandLivre grandLivre,
        NotificationService notifications,
        ComptesWalletInitialiseur initialiseur,
        PlatformTransactionManager gestionnaireTransactions,
        @Value("${app.wallet.montant-max:10000000}") BigDecimal montantMax
    ) {
        this.comptes = comptes;
        this.ecritures = ecritures;
        this.deviseRepository = deviseRepository;
        this.tauxRepository = tauxRepository;
        this.transferts = transferts;
        this.devisService = devisService;
        this.grandLivre = grandLivre;
        this.notifications = notifications;
        this.initialiseur = initialiseur;
        this.transaction = new TransactionTemplate(gestionnaireTransactions);
        this.montantMax = montantMax;
    }

    // ---------- Lecture ----------

    public WalletResponse lire(Utilisateur utilisateur) {
        preparer(utilisateur);
        return instantane(utilisateur.getId());
    }

    public List<MouvementResponse> mouvements(Utilisateur utilisateur, int limite) {
        List<MouvementResponse> resultat = new ArrayList<>();
        for (EcritureWallet e : ecritures.recentes(utilisateur.getId(), CompteWallet.Type.UTILISATEUR, PageRequest.of(0, limite))) {
            MouvementResponse m = new MouvementResponse();
            m.id = e.getId();
            m.reference = e.getOperation().getReference();
            m.type = e.getOperation().getType().name();
            m.devise = e.getCompte().getDevise().getCode();
            m.decimales = e.getCompte().getDevise().getDecimales();
            m.montant = e.getSens() == EcritureWallet.Sens.CREDIT ? e.getMontant() : e.getMontant().negate();
            m.soldeApres = e.getSoldeApres();
            m.date = e.getDateCreation();
            m.taux = e.getOperation().getTauxApplique();
            resultat.add(m);
        }
        return resultat;
    }

    // ---------- Opérations ----------

    public WalletResponse deposer(Utilisateur utilisateur, String codeDevise, BigDecimal montant) {
        preparer(utilisateur);
        return transaction.execute(statut -> {
            Devise devise = deviseActive(codeDevise);
            BigDecimal m = normaliser(montant, devise);
            CompteWallet compte = compteUtilisateur(utilisateur.getId(), devise.getCode());
            CompteWallet externe = compteSysteme(CompteWallet.Type.EXTERNE, devise.getCode());
            Map<Long, CompteWallet> verrouilles = verrouiller(compte, externe);

            grandLivre.passer(OperationWallet.Type.DEPOT, utilisateur, null, List.of(
                ligne(verrouilles, externe, EcritureWallet.Sens.DEBIT, m),
                ligne(verrouilles, compte, EcritureWallet.Sens.CREDIT, m)
            ));
            notifications.creer(utilisateur.getId(), Notification.Type.DEPOT, null, null, m, devise.getCode(), null, null);
            return instantane(utilisateur.getId());
        });
    }

    public WalletResponse retirer(Utilisateur utilisateur, String codeDevise, BigDecimal montant) {
        preparer(utilisateur);
        return transaction.execute(statut -> {
            Devise devise = deviseActive(codeDevise);
            BigDecimal m = normaliser(montant, devise);
            CompteWallet compte = compteUtilisateur(utilisateur.getId(), devise.getCode());
            CompteWallet externe = compteSysteme(CompteWallet.Type.EXTERNE, devise.getCode());
            Map<Long, CompteWallet> verrouilles = verrouiller(compte, externe);

            grandLivre.passer(OperationWallet.Type.RETRAIT, utilisateur, null, List.of(
                ligne(verrouilles, compte, EcritureWallet.Sens.DEBIT, m),
                ligne(verrouilles, externe, EcritureWallet.Sens.CREDIT, m)
            ));
            notifications.creer(utilisateur.getId(), Notification.Type.RETRAIT, null, null, m, devise.getCode(), null, null);
            return instantane(utilisateur.getId());
        });
    }

    public WalletResponse convertir(Utilisateur utilisateur, String codeSource, String codeCible, BigDecimal montant) {
        preparer(utilisateur);
        return transaction.execute(statut -> {
            Devise source = deviseActive(codeSource);
            Devise cible = deviseActive(codeCible);
            if (source.getCode().equals(cible.getCode())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.devises.identiques");
            }
            BigDecimal m = normaliser(montant, source);

            DevisResponse devis = devisService.calculer(source.getCode(), cible.getCode(), m, DevisService.Mode.COFFRE);
            if (devis.montantRecu.signum() <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.montant.trop.faible");
            }

            CompteWallet utilisateurSource = compteUtilisateur(utilisateur.getId(), source.getCode());
            CompteWallet utilisateurCible = compteUtilisateur(utilisateur.getId(), cible.getCode());
            CompteWallet coffreSource = compteSysteme(CompteWallet.Type.COFFRE, source.getCode());
            CompteWallet coffreCible = compteSysteme(CompteWallet.Type.COFFRE, cible.getCode());
            Map<Long, CompteWallet> v = verrouiller(utilisateurSource, utilisateurCible, coffreSource, coffreCible);

            // L'utilisateur paie le montant envoyé (frais compris) au coffre, et le coffre lui verse le montant converti.
            OperationWallet operation = grandLivre.passer(OperationWallet.Type.CONVERSION, utilisateur, devis.tauxApplique, List.of(
                ligne(v, utilisateurSource, EcritureWallet.Sens.DEBIT, devis.montantEnvoye),
                ligne(v, coffreSource, EcritureWallet.Sens.CREDIT, devis.montantEnvoye),
                ligne(v, coffreCible, EcritureWallet.Sens.DEBIT, devis.montantRecu),
                ligne(v, utilisateurCible, EcritureWallet.Sens.CREDIT, devis.montantRecu)
            ));

            // Commission de RucoMali : les frais, plus la marge (écart entre le taux de référence et le taux appliqué).
            BigDecimal margeCible = devis.montantEnvoye.subtract(devis.frais).multiply(devis.tauxReference).subtract(devis.montantRecu);
            if (margeCible.signum() < 0) margeCible = BigDecimal.ZERO;
            BigDecimal commission = enUsd(source.getCode(), devis.frais).add(enUsd(cible.getCode(), margeCible));
            operation.setCommissionUsd(commission.setScale(4, java.math.RoundingMode.HALF_UP));

            notifications.creer(
                utilisateur.getId(), Notification.Type.CONVERSION, null, null,
                devis.montantEnvoye, source.getCode(), devis.montantRecu, cible.getCode()
            );
            return instantane(utilisateur.getId());
        });
    }

    /**
     * Envoie de l'argent à un bénéficiaire : le solde de l'expéditeur est débité, le coffre RucoMali encaisse
     * le montant envoyé et verse le montant converti au partenaire de paiement (le « monde extérieur » simulé),
     * qui le remet au bénéficiaire (MaxIt ou banque). Tout se fait dans une seule transaction.
     */
    public Transfert creerTransfert(Utilisateur utilisateur, Beneficiaire beneficiaire, String codeSource, String codeCible, BigDecimal montant) {
        preparer(utilisateur);
        return transaction.execute(statut -> {
            Devise source = deviseActive(codeSource);
            Devise cible = deviseActive(codeCible);
            if (source.getCode().equals(cible.getCode())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.devises.identiques");
            }
            BigDecimal m = normaliser(montant, source);

            DevisResponse devis = devisService.calculer(source.getCode(), cible.getCode(), m, DevisService.Mode.COFFRE);
            if (devis.montantRecu.signum() <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.montant.trop.faible");
            }

            CompteWallet utilisateurSource = compteUtilisateur(utilisateur.getId(), source.getCode());
            CompteWallet coffreSource = compteSysteme(CompteWallet.Type.COFFRE, source.getCode());
            CompteWallet coffreCible = compteSysteme(CompteWallet.Type.COFFRE, cible.getCode());
            CompteWallet externeCible = compteSysteme(CompteWallet.Type.EXTERNE, cible.getCode());
            Map<Long, CompteWallet> v = verrouiller(utilisateurSource, coffreSource, coffreCible, externeCible);

            OperationWallet operation = grandLivre.passer(OperationWallet.Type.TRANSFERT, utilisateur, devis.tauxApplique, List.of(
                ligne(v, utilisateurSource, EcritureWallet.Sens.DEBIT, devis.montantEnvoye),
                ligne(v, coffreSource, EcritureWallet.Sens.CREDIT, devis.montantEnvoye),
                ligne(v, coffreCible, EcritureWallet.Sens.DEBIT, devis.montantRecu),
                ligne(v, externeCible, EcritureWallet.Sens.CREDIT, devis.montantRecu)
            ));

            BigDecimal margeCible = devis.montantEnvoye.subtract(devis.frais).multiply(devis.tauxReference).subtract(devis.montantRecu);
            if (margeCible.signum() < 0) margeCible = BigDecimal.ZERO;
            BigDecimal commission = enUsd(source.getCode(), devis.frais).add(enUsd(cible.getCode(), margeCible));
            operation.setCommissionUsd(commission.setScale(4, java.math.RoundingMode.HALF_UP));

            Transfert t = new Transfert();
            t.setExpediteur(utilisateur);
            t.setBeneficiaireId(beneficiaire.getId());
            t.setBeneficiaireNom(beneficiaire.getNomComplet());
            t.setBeneficiairePays(beneficiaire.getPays().name());
            t.setBeneficiaireMoyen(beneficiaire.getMoyenReception().name());
            t.setBeneficiaireIdentifiant(beneficiaire.getIdentifiantReception());
            t.setDeviseSource(source.getCode());
            t.setMontantSource(devis.montantEnvoye);
            t.setFrais(devis.frais);
            t.setDeviseCible(cible.getCode());
            t.setMontantCible(devis.montantRecu);
            t.setTauxApplique(devis.tauxApplique);
            t.setOperationReference(operation.getReference());
            return transferts.save(t);
        });
    }

    /** Réapprovisionne le coffre de RucoMali (simulé : la contrepartie est le « monde extérieur »). */
    public void apporterAuCoffre(Utilisateur administrateur, String codeDevise, BigDecimal montant) {
        initialiseur.assurerComptesSysteme();
        transaction.executeWithoutResult(statut -> {
            Devise devise = deviseActive(codeDevise);
            BigDecimal m = normaliser(montant, devise, MONTANT_MAX_APPORT);
            CompteWallet coffre = compteSysteme(CompteWallet.Type.COFFRE, devise.getCode());
            CompteWallet externe = compteSysteme(CompteWallet.Type.EXTERNE, devise.getCode());
            Map<Long, CompteWallet> verrouilles = verrouiller(coffre, externe);

            grandLivre.passer(OperationWallet.Type.APPORT_COFFRE, administrateur, null, List.of(
                ligne(verrouilles, externe, EcritureWallet.Sens.DEBIT, m),
                ligne(verrouilles, coffre, EcritureWallet.Sens.CREDIT, m)
            ));
        });
    }

    // ---------- Outils ----------

    /** Valeur d'un montant en dollars, au dernier taux connu (0 si le taux manque). */
    private BigDecimal enUsd(String codeDevise, BigDecimal montant) {
        if ("USD".equals(codeDevise)) return montant;
        return tauxRepository
            .findFirstByDeviseSource_CodeAndDeviseCible_CodeOrderByDateMiseAJourDesc(codeDevise, "USD")
            .map(t -> montant.multiply(t.getTaux()))
            .orElse(BigDecimal.ZERO);
    }

    private void preparer(Utilisateur utilisateur) {
        initialiseur.assurerComptesSysteme();
        initialiseur.assurerComptesUtilisateur(utilisateur.getId());
    }

    private Devise deviseActive(String code) {
        if (code == null) throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.devise.inconnue");
        return deviseRepository.findById(code.trim().toUpperCase(Locale.ROOT))
            .filter(Devise::isActive)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "erreur.devise.inconnue"));
    }

    /** Vérifie le montant (positif, pas trop grand, pas plus de décimales que la devise n'en gère). */
    private BigDecimal normaliser(BigDecimal montant, Devise devise) {
        return normaliser(montant, devise, montantMax);
    }

    private BigDecimal normaliser(BigDecimal montant, Devise devise, BigDecimal maximum) {
        if (montant == null || montant.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.montant.invalide");
        }
        if (montant.stripTrailingZeros().scale() > devise.getDecimales()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.decimales.invalides");
        }
        if (montant.compareTo(maximum) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.montant.trop.eleve");
        }
        return montant.setScale(devise.getDecimales(), java.math.RoundingMode.UNNECESSARY);
    }

    private CompteWallet compteUtilisateur(Long utilisateurId, String codeDevise) {
        return comptes.findByProprietaireIdAndDevise_CodeAndType(utilisateurId, codeDevise, CompteWallet.Type.UTILISATEUR)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "erreur.devise.inconnue"));
    }

    private CompteWallet compteSysteme(CompteWallet.Type type, String codeDevise) {
        return comptes.findByTypeAndProprietaireIsNullAndDevise_Code(type, codeDevise)
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "erreur.devise.inconnue"));
    }

    private Map<Long, CompteWallet> verrouiller(CompteWallet... aVerrouiller) {
        List<Long> ids = new ArrayList<>();
        for (CompteWallet c : aVerrouiller) {
            if (!ids.contains(c.getId())) ids.add(c.getId());
        }
        Map<Long, CompteWallet> parId = new HashMap<>();
        for (CompteWallet c : comptes.verrouiller(ids)) {
            parId.put(c.getId(), c);
        }
        return parId;
    }

    private Ligne ligne(Map<Long, CompteWallet> verrouilles, CompteWallet compte, EcritureWallet.Sens sens, BigDecimal montant) {
        return new Ligne(verrouilles.get(compte.getId()), sens, montant);
    }

    private int rang(String code) {
        int i = ORDRE.indexOf(code);
        return i < 0 ? ORDRE.size() : i;
    }

    private WalletResponse instantane(Long utilisateurId) {
        List<CompteWallet> liste = new ArrayList<>(
            comptes.findByProprietaireIdAndType(utilisateurId, CompteWallet.Type.UTILISATEUR)
        );
        liste.removeIf(c -> !c.getDevise().isActive());
        liste.sort(Comparator.comparingInt((CompteWallet c) -> rang(c.getDevise().getCode()))
            .thenComparing(c -> c.getDevise().getCode()));

        WalletResponse reponse = new WalletResponse();
        reponse.comptes = new ArrayList<>();
        for (CompteWallet c : liste) {
            CompteResponse cr = new CompteResponse();
            cr.devise = c.getDevise().getCode();
            cr.nom = c.getDevise().getNom();
            cr.symbole = c.getDevise().getSymbole();
            cr.decimales = c.getDevise().getDecimales();
            cr.solde = c.getSolde();
            reponse.comptes.add(cr);
        }
        return reponse;
    }
}
