package com.knapsack.service;

import com.knapsack.model.Cart;
import com.knapsack.model.Product;
import com.knapsack.model.User;
import com.knapsack.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<Cart> getCartItems(User user) {
        return cartRepository.findByUser_UserId(user.getUserId());
    }

    public Cart addToCart(User user, Product product, Integer quantity) {
        Optional<Cart> existingOpt = cartRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), product.getProductId());

        if (existingOpt.isPresent()) {
            Cart existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + (quantity != null ? quantity : 1));
            return cartRepository.save(existing);
        } else {
            Cart newItem = new Cart(user, product, quantity != null ? quantity : 1);
            return cartRepository.save(newItem);
        }
    }

    public void updateQuantity(Integer cartId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            cartRepository.deleteById(cartId);
        } else {
            cartRepository.findById(cartId).ifPresent(item -> {
                item.setQuantity(quantity);
                cartRepository.save(item);
            });
        }
    }

    public void removeFromCart(Integer cartId) {
        cartRepository.deleteById(cartId);
    }

    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser_UserId(user.getUserId());
    }

    public Double calculateSubtotal(List<Cart> items) {
        return items.stream()
                .mapToDouble(Cart::getSubtotal)
                .sum();
    }
}
