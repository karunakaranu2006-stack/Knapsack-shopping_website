package com.knapsack.service;

import com.knapsack.model.Order;
import com.knapsack.model.Product;

import com.knapsack.repository.OrderRepository;
import com.knapsack.repository.ProductRepository;
import com.knapsack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public AdminReportService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> generateAnalyticalReport() {
        Map<String, Object> report = new HashMap<>();

        List<Order> allOrders = orderRepository.findAll();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = allOrders.size();

        double totalRevenue = allOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
                .mapToDouble(Order::getFinalAmount)
                .sum();

        long placedCount = allOrders.stream().filter(o -> "PLACED".equals(o.getOrderStatus())).count();
        long processingCount = allOrders.stream().filter(o -> "PROCESSING".equals(o.getOrderStatus())).count();
        long shippedCount = allOrders.stream().filter(o -> "SHIPPED".equals(o.getOrderStatus())).count();
        long deliveredCount = allOrders.stream().filter(o -> "DELIVERED".equals(o.getOrderStatus())).count();
        long cancelledCount = allOrders.stream().filter(o -> "CANCELLED".equals(o.getOrderStatus())).count();

        List<Product> featuredProducts = productRepository.findByIsFeaturedTrue();

        report.put("totalUsers", totalUsers);
        report.put("totalProducts", totalProducts);
        report.put("totalOrders", totalOrders);
        report.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        report.put("placedCount", placedCount);
        report.put("processingCount", processingCount);
        report.put("shippedCount", shippedCount);
        report.put("deliveredCount", deliveredCount);
        report.put("cancelledCount", cancelledCount);
        report.put("featuredCount", featuredProducts.size());

        return report;
    }
}
