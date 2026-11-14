package com.knapsack.controller;

import com.knapsack.model.Product;
import com.knapsack.model.User;
import com.knapsack.service.ProductService;
import com.knapsack.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;

    @Autowired
    public ReviewController(ReviewService reviewService, ProductService productService) {
        this.reviewService = reviewService;
        this.productService = productService;
    }

    @PostMapping("/add")
    public String addReview(@RequestParam("productId") Integer productId,
                            @RequestParam("rating") Integer rating,
                            @RequestParam("reviewText") String reviewText,
                            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/products/" + productId;
        }

        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isPresent()) {
            reviewService.addReview(currentUser, productOpt.get(), rating, reviewText);
        }

        return "redirect:/products/" + productId;
    }
}
