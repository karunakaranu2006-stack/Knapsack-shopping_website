package com.knapsack.controller;

import com.knapsack.model.Address;
import com.knapsack.model.RewardHistory;
import com.knapsack.model.RewardPoint;
import com.knapsack.model.User;
import com.knapsack.service.AddressService;
import com.knapsack.service.CartService;
import com.knapsack.service.RewardService;
import com.knapsack.service.UserService;
import com.knapsack.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final AddressService addressService;
    private final RewardService rewardService;
    private final CartService cartService;
    private final WishlistService wishlistService;

    @Autowired
    public ProfileController(UserService userService,
                             AddressService addressService,
                             RewardService rewardService,
                             CartService cartService,
                             WishlistService wishlistService) {
        this.userService = userService;
        this.addressService = addressService;
        this.rewardService = rewardService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String viewProfile(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/profile";
        }

        List<Address> addresses = addressService.getUserAddresses(currentUser);
        RewardPoint rewardPoint = rewardService.getRewardPoint(currentUser);
        List<RewardHistory> rewardHistories = rewardService.getRewardHistory(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("addresses", addresses);
        model.addAttribute("rewardPoint", rewardPoint);
        model.addAttribute("rewardHistories", rewardHistories);
        model.addAttribute("cartCount", cartService.getCartItems(currentUser).size());
        model.addAttribute("wishlistCount", wishlistService.getWishlistItems(currentUser).size());

        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                HttpSession session,
                                Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        User updatedUser = userService.updateUserProfile(currentUser, fullName, phone);
        session.setAttribute("currentUser", updatedUser);

        return "redirect:/profile?success=profile_updated";
    }
}
