package com.malirussia.transfer.service;

import com.malirussia.transfer.dto.TransfertDtos.TransfertRequest;
import com.malirussia.transfer.dto.TransfertDtos.TransfertResponse;
import com.malirussia.transfer.exception.ApiException;
import com.malirussia.transfer.model.Beneficiaire;
import com.malirussia.transfer.model.Transfert;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.BeneficiaireRepository;
import com.malirussia.transfer.repository.DeviseRepository;
import com.malirussia.transfer.repository.TransfertRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TransfertService {

    private final BeneficiaireRepository beneficiaireRepository;
    private final TransfertRepository transferts;
    private final DeviseRepository deviseRepository;
    private final PinService pinService;
    private final WalletService walletService;

    public TransfertService(
        BeneficiaireRepository beneficiaireRepository,
        TransfertRepository transferts,
        DeviseRepository deviseRepository,
        PinService pinService,
        WalletService walletService
    ) {
        this.beneficiaireRepository = beneficiaireRepository;
        this.transferts = transferts;
        this.deviseRepository = deviseRepository;
        this.pinService = pinService;
        this.walletService = walletService;
    }

    public TransfertResponse creer(Utilisateur utilisateur, TransfertRequest requete) {
        Beneficiaire beneficiaire = beneficiaireRepository
            .findByIdAndProprietaireId(requete.beneficiaireId, utilisateur.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "erreur.beneficiaire.introuvable"));

        // Le bénéficiaire reçoit dans la monnaie de son pays : XOF au Mali, RUB en Russie.
        String attendue = beneficiaire.getPays() == Utilisateur.Pays.MALI ? "XOF" : "RUB";
        if (!attendue.equals(requete.deviseCible.trim().toUpperCase(Locale.ROOT))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "erreur.beneficiaire.pays.incoherent");
        }

        pinService.verifier(utilisateur.getId(), requete.pin);

        Transfert transfert = walletService.creerTransfert(
            utilisateur, beneficiaire, requete.deviseSource, requete.deviseCible, requete.montant
        );
        return versReponse(transfert);
    }

    public List<TransfertResponse> lister(Utilisateur utilisateur, int limite) {
        List<TransfertResponse> resultat = new ArrayList<>();
        for (Transfert t : transferts.findByExpediteurIdOrderByIdDesc(utilisateur.getId(), PageRequest.of(0, limite))) {
            resultat.add(versReponse(t));
        }
        return resultat;
    }

    public TransfertResponse lire(Utilisateur utilisateur, String reference) {
        return transferts.findByReferenceAndExpediteurId(reference, utilisateur.getId())
            .map(this::versReponse)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "erreur.transfert.introuvable"));
    }

    private TransfertResponse versReponse(Transfert t) {
        TransfertResponse r = new TransfertResponse();
        r.reference = t.getReference();
        r.statut = t.getStatut().name();
        r.beneficiaireNom = t.getBeneficiaireNom();
        r.beneficiairePays = t.getBeneficiairePays();
        r.beneficiaireMoyen = t.getBeneficiaireMoyen();
        r.beneficiaireIdentifiant = t.getBeneficiaireIdentifiant();
        r.deviseSource = t.getDeviseSource();
        r.decimalesSource = deviseRepository.findById(t.getDeviseSource()).map(d -> d.getDecimales()).orElse(2);
        r.montantSource = t.getMontantSource();
        r.frais = t.getFrais();
        r.deviseCible = t.getDeviseCible();
        r.decimalesCible = deviseRepository.findById(t.getDeviseCible()).map(d -> d.getDecimales()).orElse(2);
        r.montantCible = t.getMontantCible();
        r.tauxApplique = t.getTauxApplique();
        r.dateCreation = t.getDateCreation();
        r.dateMiseAJour = t.getDateMiseAJour();
        return r;
    }
}
