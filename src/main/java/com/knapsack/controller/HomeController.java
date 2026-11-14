package com.knapsack.controller;

import com.knapsack.model.User;
import com.knapsack.service.CategoryService;
import com.knapsack.service.ProductService;
import com.knapsack.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;

    @Autowired
    public HomeController(ProductService productService, CategoryService categoryService, CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("allProducts", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());

        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        if (currentUser != null) {
            model.addAttribute("cartCount", cartService.getCartItems(currentUser).size());
        } else {
            model.addAttribute("cartCount", 0);
        }

        return "index";
    }

    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", currentUser != null ? cartService.getCartItems(currentUser).size() : 0);
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", currentUser != null ? cartService.getCartItems(currentUser).size() : 0);
        return "contact";
    }
}
