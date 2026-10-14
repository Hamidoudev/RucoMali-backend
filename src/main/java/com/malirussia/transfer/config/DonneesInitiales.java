package com.malirussia.transfer.config;

import com.malirussia.transfer.model.Devise;
import com.malirussia.transfer.repository.DeviseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Crée les quatre devises du coffre au premier démarrage. */
@Component
public class DonneesInitiales implements CommandLineRunner {

    private final DeviseRepository deviseRepository;

    public DonneesInitiales(DeviseRepository deviseRepository) {
        this.deviseRepository = deviseRepository;
    }

    @Override
    public void run(String... args) {
        if (deviseRepository.count() > 0) return;
        deviseRepository.saveAll(List.of(
            new Devise("RUB", "Rouble russe", "₽", 2),
            new Devise("XOF", "Franc CFA (UEMOA)", "FCFA", 0),
            new Devise("USD", "Dollar américain", "$", 2),
            new Devise("EUR", "Euro", "€", 2)
        ));
    }
}
