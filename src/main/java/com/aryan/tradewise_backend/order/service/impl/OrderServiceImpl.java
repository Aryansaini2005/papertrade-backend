package com.aryan.tradewise_backend.order.service.impl;

import com.aryan.tradewise_backend.market.entity.Asset;
import com.aryan.tradewise_backend.market.repository.AssetRepository;
import com.aryan.tradewise_backend.order.dto.CreateOrderRequest;
import com.aryan.tradewise_backend.order.dto.OrderResponse;
import com.aryan.tradewise_backend.order.entity.Order;
import com.aryan.tradewise_backend.order.enums.OrderStatus;
import com.aryan.tradewise_backend.order.enums.OrderType;
import com.aryan.tradewise_backend.order.repository.OrderRepository;
import com.aryan.tradewise_backend.order.service.OrderService;
import com.aryan.tradewise_backend.security.CurrentUserService;
import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.entity.Wallet;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import com.aryan.tradewise_backend.user.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CurrentUserService currentUserService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            AssetRepository assetRepository,
            UserRepository userRepository,
            WalletRepository walletRepository,
            CurrentUserService currentUserService) {

        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

        String email = currentUserService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Asset asset = assetRepository
                .findBySymbol(request.getAssetSymbol().toUpperCase())
                .orElseThrow(() ->
                        new RuntimeException("Asset not found"));

        if (!asset.isActive()) {
            throw new RuntimeException("Asset is currently inactive");
        }

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found"));

        BigDecimal orderAmount = asset.getCurrentPrice()
                .multiply(request.getQuantity());

        if (request.getOrderType() == OrderType.BUY) {

            if (wallet.getAvailableBalance()
                    .compareTo(orderAmount) < 0) {

                throw new RuntimeException("Insufficient available balance");
            }

            wallet.setAvailableBalance(
                    wallet.getAvailableBalance()
                            .subtract(orderAmount)
            );

            wallet.setLockedBalance(
                    wallet.getLockedBalance()
                            .add(orderAmount)
            );

            walletRepository.save(wallet);
        }

        Order order = Order.builder()
                .user(user)
                .asset(asset)
                .orderType(request.getOrderType())
                .quantity(request.getQuantity())
                .price(asset.getCurrentPrice())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .assetSymbol(savedOrder.getAsset().getSymbol())
                .orderType(savedOrder.getOrderType())
                .quantity(savedOrder.getQuantity())
                .price(savedOrder.getPrice())
                .status(savedOrder.getStatus())
                .createdAt(savedOrder.getCreatedAt())
                .build();
    }
    @Override
    public List<OrderResponse> getMyOrders() {

        String email = currentUserService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return orderRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(order -> OrderResponse.builder()
                        .id(order.getId())
                        .assetSymbol(order.getAsset().getSymbol())
                        .orderType(order.getOrderType())
                        .quantity(order.getQuantity())
                        .price(order.getPrice())
                        .status(order.getStatus())
                        .createdAt(order.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        String email = currentUserService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        if (order.getOrderType() == OrderType.BUY) {

            BigDecimal orderAmount = order.getPrice()
                    .multiply(order.getQuantity());

            Wallet wallet = walletRepository.findByUser(user)
                    .orElseThrow(() ->
                            new RuntimeException("Wallet not found"));

            wallet.setLockedBalance(
                    wallet.getLockedBalance()
                            .subtract(orderAmount)
            );

            wallet.setAvailableBalance(
                    wallet.getAvailableBalance()
                            .add(orderAmount)
            );

            walletRepository.save(wallet);
        }
        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .assetSymbol(savedOrder.getAsset().getSymbol())
                .orderType(savedOrder.getOrderType())
                .quantity(savedOrder.getQuantity())
                .price(savedOrder.getPrice())
                .status(savedOrder.getStatus())
                .createdAt(savedOrder.getCreatedAt())
                .build();
    }
}