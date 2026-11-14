package com.knapsack.service;

import com.knapsack.model.Cart;
import com.knapsack.model.Order;
import com.knapsack.model.OrderItem;
import com.knapsack.model.Product;
import com.knapsack.model.User;
import com.knapsack.repository.OrderRepository;
import com.knapsack.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final RewardService rewardService;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CartService cartService,
                        RewardService rewardService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.rewardService = rewardService;
    }

    @Transactional
    public Order createOrder(User user, String shippingAddress, String paymentMethod, int pointsToRedeem) {
        List<Cart> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        double totalAmount = cartService.calculateSubtotal(cartItems);
        
        // Calculate reward discount: 100 points = ₹500 discount
        double discountAmount = (pointsToRedeem / 100.0) * 500.0;
        if (discountAmount > totalAmount) {
            discountAmount = totalAmount;
        }
        double finalAmount = totalAmount - discountAmount;

        String orderRef = "KNS-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderRef);
        order.setUser(user);
        order.setTotalAmount(Math.round(totalAmount * 100.0) / 100.0);
        order.setDiscountAmount(Math.round(discountAmount * 100.0) / 100.0);
        order.setFinalAmount(Math.round(finalAmount * 100.0) / 100.0);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "CREDIT_CARD");
        order.setOrderStatus("PLACED");
        order.setOrderDate(LocalDateTime.now());

        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // Deduct stock quantity
            int updatedStock = Math.max(0, product.getStockQuantity() - cartItem.getQuantity());
            product.setStockQuantity(updatedStock);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    product.getEffectivePrice()
            );
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Deduct redeemed reward points
        if (pointsToRedeem > 0) {
            rewardService.redeemPoints(user, pointsToRedeem, savedOrder.getOrderNumber());
        }

        // Clear cart
        cartService.clearCart(user);

        return savedOrder;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId());
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Order> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional
    public Order updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public boolean cancelOrder(Integer orderId, User user) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (!order.getUser().getUserId().equals(user.getUserId()) && !"ADMIN".equals(user.getRole())) {
                return false;
            }
            if ("PLACED".equals(order.getOrderStatus()) || "PROCESSING".equals(order.getOrderStatus())) {
                order.setOrderStatus("CANCELLED");
                
                // Restore stock quantities
                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    if (product != null) {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
                
                orderRepository.save(order);
                return true;
            }
        }
        return false;
    }
}
