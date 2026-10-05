package com.aryan.tradewise_backend.order;

import com.aryan.tradewise_backend.market.entity.Asset;
import com.aryan.tradewise_backend.market.repository.AssetRepository;
import com.aryan.tradewise_backend.market.service.MarketPriceService;
import com.aryan.tradewise_backend.order.dto.CreateOrderRequest;
import com.aryan.tradewise_backend.order.dto.OrderResponse;
import com.aryan.tradewise_backend.order.entity.Order;
import com.aryan.tradewise_backend.order.enums.OrderStatus;
import com.aryan.tradewise_backend.order.enums.OrderType;
import com.aryan.tradewise_backend.order.repository.OrderRepository;
import com.aryan.tradewise_backend.order.service.impl.OrderServiceImpl;
import com.aryan.tradewise_backend.portfolio.entity.Portfolio;
import com.aryan.tradewise_backend.portfolio.repository.PortfolioRepository;
import com.aryan.tradewise_backend.security.CurrentUserService;
import com.aryan.tradewise_backend.trade.entity.Trade;
import com.aryan.tradewise_backend.trade.repository.TradeRepository;
import com.aryan.tradewise_backend.user.entity.User;
import com.aryan.tradewise_backend.user.entity.Wallet;
import com.aryan.tradewise_backend.user.repository.UserRepository;
import com.aryan.tradewise_backend.user.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private AssetRepository assetRepository;
    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private CurrentUserService currentUserService;
    private PortfolioRepository portfolioRepository;
    private TradeRepository tradeRepository;
    private MarketPriceService marketPriceService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {

        orderRepository = mock(OrderRepository.class);
        assetRepository = mock(AssetRepository.class);
        userRepository = mock(UserRepository.class);
        walletRepository = mock(WalletRepository.class);
        currentUserService = mock(CurrentUserService.class);
        portfolioRepository = mock(PortfolioRepository.class);
        tradeRepository = mock(TradeRepository.class);
        marketPriceService = mock(MarketPriceService.class);

        orderService = new OrderServiceImpl(
                orderRepository,
                assetRepository,
                userRepository,
                walletRepository,
                currentUserService,
                portfolioRepository,
                tradeRepository,
                marketPriceService
        );
    }

    @Test
    void shouldCreateBuyOrderSuccessfully() {

        User user = new User();

        Asset asset = new Asset();
        asset.setSymbol("AAPL");
        asset.setCurrentPrice(new BigDecimal("100"));
        asset.setActive(true);

        Wallet wallet = new Wallet();
        wallet.setAvailableBalance(new BigDecimal("10000"));
        wallet.setLockedBalance(BigDecimal.ZERO);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAssetSymbol("AAPL");
        request.setOrderType(OrderType.BUY);
        request.setQuantity(new BigDecimal("10"));

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(assetRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(asset));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(wallet));

        Order savedOrder = Order.builder()
                .id(1L)
                .user(user)
                .asset(asset)
                .orderType(OrderType.BUY)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("AAPL", response.getAssetSymbol());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(new BigDecimal("10"), response.getQuantity());
        assertEquals(new BigDecimal("100"), response.getPrice());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        assertEquals(
                new BigDecimal("9000"),
                wallet.getAvailableBalance()
        );

        assertEquals(
                new BigDecimal("1000"),
                wallet.getLockedBalance()
        );

        verify(walletRepository).save(wallet);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldRejectBuyOrderWhenBalanceIsInsufficient() {

        User user = new User();

        Asset asset = new Asset();
        asset.setSymbol("AAPL");
        asset.setCurrentPrice(new BigDecimal("100"));
        asset.setActive(true);

        Wallet wallet = new Wallet();
        wallet.setAvailableBalance(new BigDecimal("500"));
        wallet.setLockedBalance(BigDecimal.ZERO);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAssetSymbol("AAPL");
        request.setOrderType(OrderType.BUY);
        request.setQuantity(new BigDecimal("10"));

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(assetRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(asset));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(wallet));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(
                "Insufficient available balance",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    void shouldRejectOrderWhenAssetIsInactive() {

        User user = new User();

        Asset asset = new Asset();
        asset.setSymbol("AAPL");
        asset.setCurrentPrice(new BigDecimal("100"));
        asset.setActive(false);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAssetSymbol("AAPL");
        request.setOrderType(OrderType.BUY);
        request.setQuantity(new BigDecimal("10"));

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(assetRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(asset));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(
                "Asset is currently inactive",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldCreateSellOrderSuccessfully() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("AAPL")
                .name("AAPL")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Wallet wallet = Wallet.builder()
                .user(user)
                .availableBalance(new BigDecimal("5000"))
                .lockedBalance(BigDecimal.ZERO)
                .build();

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .asset(asset)
                .availableQuantity(new BigDecimal("20"))
                .lockedQuantity(BigDecimal.ZERO)
                .build();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAssetSymbol("AAPL");
        request.setOrderType(OrderType.SELL);
        request.setQuantity(new BigDecimal("10"));

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(assetRepository.findBySymbol("AAPL"))
                .thenReturn(Optional.of(asset));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(wallet));

        when(portfolioRepository.findByUserAndAsset(user, asset))
                .thenReturn(Optional.of(portfolio));

        Order savedOrder = Order.builder()
                .id(2L)
                .user(user)
                .asset(asset)
                .orderType(OrderType.SELL)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("AAPL", response.getAssetSymbol());
        assertEquals(OrderType.SELL, response.getOrderType());
        assertEquals(new BigDecimal("10"), response.getQuantity());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        assertEquals(
                new BigDecimal("10"),
                portfolio.getAvailableQuantity()
        );

        assertEquals(
                new BigDecimal("10"),
                portfolio.getLockedQuantity()
        );

        verify(portfolioRepository).save(portfolio);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldCancelBuyOrderSuccessfully() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Wallet wallet = Wallet.builder()
                .user(user)
                .availableBalance(new BigDecimal("9000"))
                .lockedBalance(new BigDecimal("1000"))
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(user)
                .asset(asset)
                .orderType(OrderType.BUY)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(wallet));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.cancelOrder(1L);

        assertEquals(
                OrderStatus.CANCELLED,
                order.getStatus()
        );

        assertEquals(
                new BigDecimal("10000"),
                wallet.getAvailableBalance()
        );

        assertEquals(
                BigDecimal.ZERO,
                wallet.getLockedBalance()
        );

        assertEquals(
                OrderStatus.CANCELLED,
                response.getStatus()
        );
    }

    @Test
    void shouldExecuteBuyOrderSuccessfully() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Wallet wallet = Wallet.builder()
                .user(user)
                .availableBalance(new BigDecimal("9000"))
                .lockedBalance(new BigDecimal("1000"))
                .build();

        Order order = Order.builder()
                .id(3L)
                .user(user)
                .asset(asset)
                .orderType(OrderType.BUY)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(3L))
                .thenReturn(Optional.of(order));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(wallet));

        when(portfolioRepository.findByUserAndAsset(user, asset))
                .thenReturn(Optional.empty());

        // IMPORTANT: live market price
        when(marketPriceService.getPrice("TCS"))
                .thenReturn(new BigDecimal("100"));

        when(portfolioRepository.save(any(Portfolio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(tradeRepository.save(any(Trade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.executeOrder(3L);

        assertEquals(
                OrderStatus.EXECUTED,
                order.getStatus()
        );

        assertEquals(
                new BigDecimal("10000"),
                wallet.getAvailableBalance()
        );

        assertEquals(
                BigDecimal.ZERO,
                wallet.getLockedBalance()
        );

        assertEquals(
                OrderStatus.EXECUTED,
                response.getStatus()
        );

        verify(walletRepository).save(wallet);
        verify(portfolioRepository).save(any(Portfolio.class));
        verify(tradeRepository).save(any(Trade.class));
        verify(orderRepository).save(order);
    }

    @Test
    void shouldExecuteSellOrderSuccessfully() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Wallet wallet = Wallet.builder()
                .user(user)
                .availableBalance(new BigDecimal("5000"))
                .lockedBalance(BigDecimal.ZERO)
                .build();

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .asset(asset)
                .availableQuantity(new BigDecimal("10"))
                .lockedQuantity(new BigDecimal("10"))
                .build();

        Order order = Order.builder()
                .id(4L)
                .user(user)
                .asset(asset)
                .orderType(OrderType.SELL)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(4L))
                .thenReturn(Optional.of(order));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(wallet));

        when(portfolioRepository.findByUserAndAsset(user, asset))
                .thenReturn(Optional.of(portfolio));

        // IMPORTANT: live market price
        when(marketPriceService.getPrice("TCS"))
                .thenReturn(new BigDecimal("100"));

        when(tradeRepository.save(any(Trade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.executeOrder(4L);

        assertEquals(
                OrderStatus.EXECUTED,
                order.getStatus()
        );

        assertEquals(
                new BigDecimal("10"),
                portfolio.getAvailableQuantity()
        );

        assertEquals(
                BigDecimal.ZERO,
                portfolio.getLockedQuantity()
        );

        assertEquals(
                new BigDecimal("6000"),
                wallet.getAvailableBalance()
        );

        assertEquals(
                BigDecimal.ZERO,
                wallet.getLockedBalance()
        );

        assertEquals(
                OrderStatus.EXECUTED,
                response.getStatus()
        );

        verify(portfolioRepository).save(portfolio);
        verify(walletRepository).save(wallet);
        verify(tradeRepository).save(any(Trade.class));
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectExecutionOfCancelledOrder() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Order order = Order.builder()
                .id(5L)
                .user(user)
                .asset(asset)
                .orderType(OrderType.BUY)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(5L))
                .thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.executeOrder(5L)
        );

        assertEquals(
                "Only pending orders can be executed",
                exception.getMessage()
        );

        verify(walletRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
        verify(tradeRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectCancellingAnotherUsersOrder() {

        User currentUser = User.builder()
                .id(1L)
                .email("current@gmail.com")
                .build();

        User orderOwner = User.builder()
                .id(2L)
                .email("owner@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Order order = Order.builder()
                .id(6L)
                .user(orderOwner)
                .asset(asset)
                .orderType(OrderType.BUY)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("current@gmail.com");

        when(userRepository.findByEmail("current@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(orderRepository.findById(6L))
                .thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.cancelOrder(6L)
        );

        assertEquals(
                "You are not authorized to cancel this order",
                exception.getMessage()
        );

        verify(walletRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectCancellingExecutedOrder() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Order order = Order.builder()
                .id(7L)
                .user(user)
                .asset(asset)
                .orderType(OrderType.BUY)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.EXECUTED)
                .build();

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(7L))
                .thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.cancelOrder(7L)
        );

        assertEquals(
                "Only pending orders can be cancelled",
                exception.getMessage()
        );

        verify(walletRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectExecutionWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.executeOrder(999L)
        );

        assertEquals(
                "Order not found",
                exception.getMessage()
        );

        verify(walletRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
        verify(tradeRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectCancellationWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.cancelOrder(999L)
        );

        assertEquals(
                "Order not found",
                exception.getMessage()
        );

        verify(walletRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectSellOrderWhenQuantityIsInsufficient() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .asset(asset)
                .availableQuantity(new BigDecimal("5"))
                .lockedQuantity(BigDecimal.ZERO)
                .build();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAssetSymbol("TCS");
        request.setOrderType(OrderType.SELL);
        request.setQuantity(new BigDecimal("10"));

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(assetRepository.findBySymbol("TCS"))
                .thenReturn(Optional.of(asset));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(
                        Wallet.builder()
                                .user(user)
                                .availableBalance(new BigDecimal("10000"))
                                .lockedBalance(BigDecimal.ZERO)
                                .build()
                ));

        when(portfolioRepository.findByUserAndAsset(user, asset))
                .thenReturn(Optional.of(portfolio));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(
                "Insufficient quantity",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void shouldRejectSellOrderWhenPortfolioDoesNotExist() {

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        Asset asset = Asset.builder()
                .symbol("TCS")
                .name("TCS")
                .currentPrice(new BigDecimal("100"))
                .active(true)
                .build();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAssetSymbol("TCS");
        request.setOrderType(OrderType.SELL);
        request.setQuantity(new BigDecimal("10"));

        when(currentUserService.getCurrentUserEmail())
                .thenReturn("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(assetRepository.findBySymbol("TCS"))
                .thenReturn(Optional.of(asset));

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(
                        Wallet.builder()
                                .user(user)
                                .availableBalance(new BigDecimal("10000"))
                                .lockedBalance(BigDecimal.ZERO)
                                .build()
                ));

        when(portfolioRepository.findByUserAndAsset(user, asset))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(request)
        );

        assertEquals(
                "You don't own this asset",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
    }
}