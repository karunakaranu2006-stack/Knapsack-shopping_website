package com.knapsack.controller;

import com.knapsack.model.Category;
import com.knapsack.model.Product;
import com.knapsack.model.Review;
import com.knapsack.model.User;
import com.knapsack.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final ReviewService reviewService;

    @Autowired
    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             CartService cartService,
                             WishlistService wishlistService,
                             ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "categoryId", required = false) Integer categoryId,
                               @RequestParam(value = "minPrice", required = false) Double minPrice,
                               @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                               Model model,
                               HttpSession session) {

        List<Product> products = productService.filterProducts(keyword, categoryId, minPrice, maxPrice);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", currentUser != null ? cartService.getCartItems(currentUser).size() : 0);
        model.addAttribute("wishlistCount", currentUser != null ? wishlistService.getWishlistItems(currentUser).size() : 0);

        return "products";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Integer productId, Model model, HttpSession session) {
        Optional<Product> productOpt = productService.getProductById(productId);

        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }

        Product product = productOpt.get();
        List<Review> reviews = reviewService.getProductReviews(productId);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);

        if (product.getCategory() != null) {
            List<Product> related = productService.getProductsByCategory(product.getCategory().getCategoryId());
            related.removeIf(p -> p.getProductId().equals(product.getProductId()));
            model.addAttribute("relatedProducts", related.stream().limit(4).toList());
        }

        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", currentUser != null ? cartService.getCartItems(currentUser).size() : 0);
        model.addAttribute("wishlistCount", currentUser != null ? wishlistService.getWishlistItems(currentUser).size() : 0);
        model.addAttribute("inWishlist", currentUser != null && wishlistService.isInWishlist(currentUser, productId));

        return "product-details";
    }
}
