package com.aryan.tradewise_backend.market.repository;

import com.aryan.tradewise_backend.market.entity.Asset;
import com.aryan.tradewise_backend.market.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findBySymbol(String symbol);

    List<Asset> findByActiveTrue();

    List<Asset> findByTypeAndActiveTrue(AssetType type);

    boolean existsBySymbol(String symbol);
}