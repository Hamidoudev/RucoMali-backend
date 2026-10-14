package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.AdminDtos.*;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.UtilisateurRepository;
import com.malirussia.transfer.service.AdminService;
import com.malirussia.transfer.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Routes réservées aux administrateurs (voir SecurityConfig : /api/admin/** exige le rôle ADMIN). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final WalletService walletService;
    private final UtilisateurRepository utilisateurRepository;

    public AdminController(AdminService adminService, WalletService walletService, UtilisateurRepository utilisateurRepository) {
        this.adminService = adminService;
        this.walletService = walletService;
        this.utilisateurRepository = utilisateurRepository;
    }

    private Utilisateur administrateurConnecte() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByUsername(username).orElseThrow();
    }

    @GetMapping("/apercu")
    public ApercuResponse apercu() {
        return adminService.apercu();
    }

    @GetMapping("/utilisateurs")
    public List<UtilisateurAdminResponse> utilisateurs() {
        return adminService.listerUtilisateurs();
    }

    @GetMapping("/transactions")
    public List<TransactionAdminResponse> transactions(@RequestParam(defaultValue = "50") int limite) {
        return adminService.transactions(Math.max(1, Math.min(limite, 200)));
    }

    @GetMapping("/liquidite")
    public List<LiquiditeResponse> liquidite() {
        return adminService.liquidite();
    }

    /** POST /api/admin/coffre/apport : réapprovisionne le coffre (simulé) et renvoie la liquidité à jour. */
    @PostMapping("/coffre/apport")
    public List<LiquiditeResponse> apporter(@Valid @RequestBody ApportRequest requete) {
        walletService.apporterAuCoffre(administrateurConnecte(), requete.devise, requete.montant);
        return adminService.liquidite();
    }

    @GetMapping("/taux")
    public List<TauxResponse> taux() {
        return adminService.listerTaux();
    }

    @PostMapping("/taux/rafraichir")
    public List<TauxResponse> rafraichirTaux() {
        return adminService.rafraichirTaux();
    }
}
