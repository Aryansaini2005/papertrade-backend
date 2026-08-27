package com.aryan.tradewise_backend.market.controller;

import com.aryan.tradewise_backend.market.dto.AssetResponse;
import com.aryan.tradewise_backend.market.dto.CreateAssetRequest;
import com.aryan.tradewise_backend.market.dto.UpdateAssetRequest;
import com.aryan.tradewise_backend.market.dto.UpdateAssetStatusRequest;
import com.aryan.tradewise_backend.market.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/assets")
public class AdminAssetController {

    private final AssetService assetService;

    public AdminAssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse createAsset(
            @Valid @RequestBody CreateAssetRequest request) {

        return assetService.createAsset(request);
    }

    @PutMapping("/{id}")
    public AssetResponse updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssetRequest request) {

        return assetService.updateAsset(id, request);
    }

    @PatchMapping("/{id}/status")
    public AssetResponse updateAssetStatus(
            @PathVariable Long id,
            @RequestBody UpdateAssetStatusRequest request) {

        return assetService.updateAssetStatus(id, request);
    }
}