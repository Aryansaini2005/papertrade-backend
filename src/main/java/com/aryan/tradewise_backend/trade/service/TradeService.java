package com.aryan.tradewise_backend.trade.service;

import com.aryan.tradewise_backend.trade.dto.TradeResponse;

import java.util.List;

public interface TradeService {

    List<TradeResponse> getMyTrades();
}