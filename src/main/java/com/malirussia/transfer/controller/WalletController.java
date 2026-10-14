package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.WalletDtos.ConversionRequest;
import com.malirussia.transfer.dto.WalletDtos.MouvementResponse;
import com.malirussia.transfer.dto.WalletDtos.OperationRequest;
import com.malirussia.transfer.dto.WalletDtos.WalletResponse;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UtilisateurRepository utilisateurRepository;

    public WalletController(WalletService walletService, UtilisateurRepository utilisateurRepository) {
        this.walletService = walletService;
        this.utilisateurRepository = utilisateurRepository;
    }

    private Utilisateur utilisateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    /** GET /api/wallet : le solde de chaque devise. */
    @GetMapping
    public WalletResponse lire() {
        return walletService.lire(utilisateurConnecte());
    }

    /** GET /api/wallet/mouvements?limite=20 : les derniers mouvements. */
    @GetMapping("/mouvements")
    public List<MouvementResponse> mouvements(@RequestParam(defaultValue = "20") int limite) {
        return walletService.mouvements(utilisateurConnecte(), Math.max(1, Math.min(limite, 100)));
    }

    /** POST /api/wallet/depot : ajoute des fonds (simulé). */
    @PostMapping("/depot")
    public WalletResponse deposer(@Valid @RequestBody OperationRequest requete) {
        return walletService.deposer(utilisateurConnecte(), requete.devise, requete.montant);
    }

    /** POST /api/wallet/retrait : retire des fonds (simulé). */
    @PostMapping("/retrait")
    public WalletResponse retirer(@Valid @RequestBody OperationRequest requete) {
        return walletService.retirer(utilisateurConnecte(), requete.devise, requete.montant);
    }

    /** POST /api/wallet/conversion : convertit d'une devise à l'autre via le coffre RucoMali. */
    @PostMapping("/conversion")
    public WalletResponse convertir(@Valid @RequestBody ConversionRequest requete) {
        return walletService.convertir(utilisateurConnecte(), requete.source, requete.cible, requete.montant);
    }
}
