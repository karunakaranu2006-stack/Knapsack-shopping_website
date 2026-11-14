package com.knapsack.controller;

import com.knapsack.model.Product;
import com.knapsack.model.User;
import com.knapsack.model.Wishlist;
import com.knapsack.service.CartService;
import com.knapsack.service.ProductService;
import com.knapsack.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final ProductService productService;
    private final CartService cartService;

    @Autowired
    public WishlistController(WishlistService wishlistService, ProductService productService, CartService cartService) {
        this.wishlistService = wishlistService;
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping
    public String viewWishlist(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/wishlist";
        }

        List<Wishlist> wishlistItems = wishlistService.getWishlistItems(currentUser);
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", cartService.getCartItems(currentUser).size());
        model.addAttribute("wishlistCount", wishlistItems.size());

        return "wishlist";
    }

    @GetMapping("/add/{id}")
    public String addToWishlist(@PathVariable("id") Integer productId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/products/" + productId;
        }

        Optional<Product> productOpt = productService.getProductById(productId);
        productOpt.ifPresent(product -> wishlistService.addToWishlist(currentUser, product));

        return "redirect:/wishlist";
    }

    @GetMapping("/remove/{id}")
    public String removeFromWishlist(@PathVariable("id") Integer productId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        wishlistService.removeFromWishlist(currentUser, productId);
        return "redirect:/wishlist";
    }

    @GetMapping("/move-to-cart/{id}")
    public String moveToCart(@PathVariable("id") Integer productId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Product> productOpt = productService.getProductById(productId);
        productOpt.ifPresent(product -> wishlistService.moveToCart(currentUser, product));

        return "redirect:/cart";
    }
}
