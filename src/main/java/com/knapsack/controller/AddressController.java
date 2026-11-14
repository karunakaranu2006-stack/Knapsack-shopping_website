package com.knapsack.controller;

import com.knapsack.model.Address;
import com.knapsack.model.User;
import com.knapsack.service.AddressService;
import com.knapsack.service.CartService;
import com.knapsack.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addressService;
    private final CartService cartService;
    private final WishlistService wishlistService;

    @Autowired
    public AddressController(AddressService addressService, CartService cartService, WishlistService wishlistService) {
        this.addressService = addressService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String viewAddresses(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/addresses";
        }

        List<Address> addresses = addressService.getUserAddresses(currentUser);
        model.addAttribute("addresses", addresses);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cartCount", cartService.getCartItems(currentUser).size());
        model.addAttribute("wishlistCount", wishlistService.getWishlistItems(currentUser).size());

        return "addresses";
    }

    @PostMapping("/save")
    public String saveAddress(@RequestParam("fullAddress") String fullAddress,
                              @RequestParam("city") String city,
                              @RequestParam(value = "state", required = false) String state,
                              @RequestParam("pincode") String pincode,
                              @RequestParam(value = "isDefault", defaultValue = "false") Boolean isDefault,
                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        addressService.saveAddress(currentUser, fullAddress, city, state, pincode, isDefault);
        return "redirect:/addresses";
    }

    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable("id") Integer addressId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        addressService.deleteAddress(addressId);
        return "redirect:/addresses";
    }
}
