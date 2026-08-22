package com.aryan.tradewise_backend.market.service;

import com.aryan.tradewise_backend.market.dto.AssetResponse;

import java.util.List;

public interface AssetService {

    List<AssetResponse> getAllActiveAssets();

    AssetResponse getAssetBySymbol(String symbol);
}