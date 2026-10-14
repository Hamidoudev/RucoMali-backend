package com.malirussia.transfer.controller;

import com.malirussia.transfer.dto.DeviseDtos.DeviseResponse;
import com.malirussia.transfer.repository.DeviseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devises")
public class DeviseController {

    private final DeviseRepository deviseRepository;

    public DeviseController(DeviseRepository deviseRepository) {
        this.deviseRepository = deviseRepository;
    }

    /** GET /api/devises : les devises actives (RUB, XOF, USD, EUR). */
    @GetMapping
    public List<DeviseResponse> lister() {
        return deviseRepository.findByActiveTrueOrderByCodeAsc().stream().map(DeviseResponse::depuis).toList();
    }
}
