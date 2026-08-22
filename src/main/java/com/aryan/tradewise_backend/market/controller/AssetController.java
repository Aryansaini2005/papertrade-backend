package com.aryan.tradewise_backend.market.controller;

import com.aryan.tradewise_backend.market.dto.AssetResponse;
import com.aryan.tradewise_backend.market.service.AssetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<AssetResponse> getAllActiveAssets() {
        return assetService.getAllActiveAssets();
    }

    @GetMapping("/{symbol}")
    public AssetResponse getAssetBySymbol(
            @PathVariable String symbol) {

        return assetService.getAssetBySymbol(symbol);
    }
}