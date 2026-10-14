package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.TransactionDtos.TransactionResponse;
import com.malirussia.transfer.model.Utilisateur;
import com.malirussia.transfer.repository.TransactionRepository;
import com.malirussia.transfer.repository.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UtilisateurRepository utilisateurRepository;

    public TransactionController(TransactionRepository transactionRepository, UtilisateurRepository utilisateurRepository) {
        this.transactionRepository = transactionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** GET /api/transactions : historique des transactions complétées de l'utilisateur connecté. */
    @GetMapping
    public List<TransactionResponse> mesTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username).orElseThrow();
        return transactionRepository.findToutesPourUtilisateur(utilisateur.getId())
            .stream().map(TransactionResponse::depuis).toList();
    }
}
