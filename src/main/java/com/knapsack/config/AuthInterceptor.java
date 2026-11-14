package com.knapsack.config;

import com.knapsack.model.User;
import com.knapsack.service.RewardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final RewardService rewardService;

    @Autowired
    public AuthInterceptor(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser != null) {
            // Process daily login reward trigger
            rewardService.checkAndProcessDailyLoginReward(currentUser);
        }

        // Admin routes protection
        if (uri.startsWith("/admin")) {
            if (currentUser == null) {
                response.sendRedirect("/login?redirect=" + uri);
                return false;
            }
            if (!"ADMIN".equals(currentUser.getRole())) {
                response.sendRedirect("/");
                return false;
            }
        }

        // Customer protected routes
        if (uri.startsWith("/cart") || uri.startsWith("/wishlist") || uri.startsWith("/checkout") ||
            uri.startsWith("/orders") || uri.startsWith("/profile") || uri.startsWith("/addresses")) {
            if (currentUser == null) {
                response.sendRedirect("/login?redirect=" + uri);
                return false;
            }
            if (currentUser.getIsBlocked()) {
                session.invalidate();
                response.sendRedirect("/login?error=account_blocked");
                return false;
            }
        }

        return true;
    }
}
