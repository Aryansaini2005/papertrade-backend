package com.aryan.tradewise_backend.market.service.impl;

import com.aryan.tradewise_backend.ai.service.AICacheService;
import com.aryan.tradewise_backend.market.dto.AssetResponse;
import com.aryan.tradewise_backend.market.dto.CreateAssetRequest;
import com.aryan.tradewise_backend.market.dto.UpdateAssetRequest;
import com.aryan.tradewise_backend.market.dto.UpdateAssetStatusRequest;
import com.aryan.tradewise_backend.market.entity.Asset;
import com.aryan.tradewise_backend.market.repository.AssetRepository;
import com.aryan.tradewise_backend.market.service.AssetService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AICacheService aiCacheService;

    public AssetServiceImpl(
            AssetRepository assetRepository,
            AICacheService aiCacheService) {

        this.assetRepository = assetRepository;
        this.aiCacheService = aiCacheService;
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

    @Override
    public AssetResponse createAsset(CreateAssetRequest request) {
        String symbol = request.getSymbol().toUpperCase();

        if (assetRepository.findBySymbol(symbol).isPresent()) {
            throw new RuntimeException(
                    "Asset with this symbol already exists");
        }

        Asset asset = Asset.builder()
                .symbol(symbol)
                .name(request.getName())
                .exchange(request.getExchange())
                .type(request.getType())
                .currentPrice(request.getCurrentPrice())
                .active(true)
                .build();

        Asset savedAsset = assetRepository.save(asset);

        // Ensure no old cache entry remains for this symbol.
        aiCacheService.evictAnalysis(savedAsset.getSymbol());

        return mapToResponse(savedAsset);
    }

    @Override
    public AssetResponse updateAsset(
            Long id,
            UpdateAssetRequest request) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Asset not found"));

        asset.setName(request.getName());
        asset.setExchange(request.getExchange());
        asset.setType(request.getType());
        asset.setCurrentPrice(request.getCurrentPrice());

        Asset updatedAsset = assetRepository.save(asset);

        aiCacheService.evictAnalysis(updatedAsset.getSymbol());

        return mapToResponse(updatedAsset);
    }

    @Override
    public AssetResponse updateAssetStatus(
            Long id,
            UpdateAssetStatusRequest request) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Asset not found"));

        asset.setActive(request.isActive());

        Asset updatedAsset = assetRepository.save(asset);

        aiCacheService.evictAnalysis(updatedAsset.getSymbol());

        return mapToResponse(updatedAsset);
    }
}
