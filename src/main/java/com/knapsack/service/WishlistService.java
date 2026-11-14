package com.knapsack.service;

import com.knapsack.model.Product;
import com.knapsack.model.User;
import com.knapsack.model.Wishlist;
import com.knapsack.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CartService cartService;

    @Autowired
    public WishlistService(WishlistRepository wishlistRepository, CartService cartService) {
        this.wishlistRepository = wishlistRepository;
        this.cartService = cartService;
    }

    public List<Wishlist> getWishlistItems(User user) {
        return wishlistRepository.findByUser_UserId(user.getUserId());
    }

    public boolean isInWishlist(User user, Integer productId) {
        return wishlistRepository.existsByUser_UserIdAndProduct_ProductId(user.getUserId(), productId);
    }

    public void addToWishlist(User user, Product product) {
        if (!isInWishlist(user, product.getProductId())) {
            Wishlist wishlist = new Wishlist(user, product);
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional
    public void removeFromWishlist(User user, Integer productId) {
        wishlistRepository.deleteByUser_UserIdAndProduct_ProductId(user.getUserId(), productId);
    }

    @Transactional
    public void moveToCart(User user, Product product) {
        cartService.addToCart(user, product, 1);
        removeFromWishlist(user, product.getProductId());
    }
}
