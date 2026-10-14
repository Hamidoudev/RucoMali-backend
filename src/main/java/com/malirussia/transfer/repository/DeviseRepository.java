package com.malirussia.transfer.repository;

import com.malirussia.transfer.model.Devise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviseRepository extends JpaRepository<Devise, String> {

    List<Devise> findByActiveTrueOrderByCodeAsc();
}
