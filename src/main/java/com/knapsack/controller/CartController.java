package com.knapsack.controller;

import com.knapsack.model.Cart;
import com.knapsack.model.Product;
import com.knapsack.model.User;
import com.knapsack.service.CartService;
import com.knapsack.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @Autowired
    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/cart";
        }

        List<Cart> cartItems = cartService.getCartItems(currentUser);
        double subtotal = cartService.calculateSubtotal(cartItems);
        double shipping = (subtotal > 1000.0 || cartItems.isEmpty()) ? 0.0 : 150.0;
        double tax = subtotal * 0.08;
        double grandTotal = subtotal + shipping + tax;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", Math.round(subtotal * 100.0) / 100.0);
        model.addAttribute("shipping", Math.round(shipping * 100.0) / 100.0);
        model.addAttribute("tax", Math.round(tax * 100.0) / 100.0);
        model.addAttribute("grandTotal", Math.round(grandTotal * 100.0) / 100.0);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", cartItems.size());

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Integer productId,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/products/" + productId;
        }

        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isPresent()) {
            cartService.addToCart(currentUser, productOpt.get(), quantity);
        }

        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("cartItemId") Integer cartItemId,
                                 @RequestParam("quantity") Integer quantity,
                                 HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        cartService.updateQuantity(cartItemId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeItem(@PathVariable("id") Integer cartItemId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        cartService.removeFromCart(cartItemId);
        return "redirect:/cart";
    }
}
