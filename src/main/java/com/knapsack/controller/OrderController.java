package com.knapsack.controller;

import com.knapsack.model.Order;
import com.knapsack.model.User;
import com.knapsack.service.CartService;
import com.knapsack.service.OrderService;
import com.knapsack.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final WishlistService wishlistService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService, WishlistService wishlistService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String userOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/orders";
        }

        List<Order> orders = orderService.getUserOrders(currentUser);
        model.addAttribute("orders", orders);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", cartService.getCartItems(currentUser).size());
        model.addAttribute("wishlistCount", wishlistService.getWishlistItems(currentUser).size());

        return "orders";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Integer orderId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }

        Order order = orderOpt.get();
        if (!order.getUser().getUserId().equals(currentUser.getUserId()) && !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", cartService.getCartItems(currentUser).size());
        model.addAttribute("wishlistCount", wishlistService.getWishlistItems(currentUser).size());

        return "order-details";
    }

    @GetMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Integer orderId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        orderService.cancelOrder(orderId, currentUser);
        return "redirect:/orders";
    }
}
