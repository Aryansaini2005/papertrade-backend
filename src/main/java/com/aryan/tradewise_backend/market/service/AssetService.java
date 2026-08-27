package com.aryan.tradewise_backend.market.service;

import com.aryan.tradewise_backend.market.dto.AssetResponse;
import com.aryan.tradewise_backend.market.dto.CreateAssetRequest;
import com.aryan.tradewise_backend.market.dto.UpdateAssetRequest;
import com.aryan.tradewise_backend.market.dto.UpdateAssetStatusRequest;

import java.util.List;

public interface AssetService {

    List<AssetResponse> getAllActiveAssets();

    AssetResponse getAssetBySymbol(String symbol);

    AssetResponse createAsset(CreateAssetRequest request);

    AssetResponse updateAsset(Long id, UpdateAssetRequest request);

    AssetResponse updateAssetStatus(
            Long id,
            UpdateAssetStatusRequest request);
}