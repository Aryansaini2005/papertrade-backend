package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.market.dto.AssetResponse;
import com.aryan.tradewise_backend.market.entity.Asset;
import com.aryan.tradewise_backend.market.repository.AssetRepository;
import com.aryan.tradewise_backend.market.service.AssetService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public List<AssetResponse> getAllActiveAssets() {

        return assetRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AssetResponse getAssetBySymbol(String symbol) {

        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() ->
                        new RuntimeException("Asset not found"));

        return mapToResponse(asset);
    }

    private AssetResponse mapToResponse(Asset asset) {

        return AssetResponse.builder()
                .id(asset.getId())
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .exchange(asset.getExchange())
                .type(asset.getType())
                .currentPrice(asset.getCurrentPrice())
                .active(asset.isActive())
                .build();
    }
}