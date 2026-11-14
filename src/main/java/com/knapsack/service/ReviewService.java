package com.knapsack.service;

import com.knapsack.model.Product;
import com.knapsack.model.Review;
import com.knapsack.model.User;
import com.knapsack.repository.ProductRepository;
import com.knapsack.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public List<Review> getProductReviews(Integer productId) {
        return reviewRepository.findByProduct_ProductIdOrderByReviewDateDesc(productId);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByReviewDateDesc();
    }

    public Review addReview(User user, Product product, Integer rating, String reviewText) {
        Review review = new Review(user, product, rating, reviewText);
        Review saved = reviewRepository.save(review);

        // Update product average rating and review count
        List<Review> allProdReviews = getProductReviews(product.getProductId());
        double avg = allProdReviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
        product.setRating(Math.round(avg * 10.0) / 10.0);
        product.setReviewCount(allProdReviews.size());
        productRepository.save(product);

        return saved;
    }

    public void deleteReview(Integer reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
