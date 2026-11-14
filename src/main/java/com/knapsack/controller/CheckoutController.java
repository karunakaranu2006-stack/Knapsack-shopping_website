package com.knapsack.controller;

import com.knapsack.model.Address;
import com.knapsack.model.Cart;
import com.knapsack.model.Order;
import com.knapsack.model.RewardPoint;
import com.knapsack.model.User;
import com.knapsack.service.AddressService;
import com.knapsack.service.CartService;
import com.knapsack.service.OrderService;
import com.knapsack.service.RewardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final RewardService rewardService;

    @Autowired
    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              AddressService addressService,
                              RewardService rewardService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.addressService = addressService;
        this.rewardService = rewardService;
    }

    @GetMapping
    public String checkoutRedirect() {
        return "redirect:/checkout/step1";
    }

    @GetMapping("/step1")
    public String checkoutStep1(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/checkout/step1";
        }

        List<Cart> cartItems = cartService.getCartItems(currentUser);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        List<Address> addresses = addressService.getUserAddresses(currentUser);
        double subtotal = cartService.calculateSubtotal(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", Math.round(subtotal * 100.0) / 100.0);
        model.addAttribute("currentUser", currentUser);

        return "checkout-step1";
    }

    @PostMapping("/confirm")
    public String checkoutConfirm(@RequestParam(value = "addressId", required = false) Integer addressId,
                                  @RequestParam(value = "newFullAddress", required = false) String newFullAddress,
                                  @RequestParam(value = "newCity", required = false) String newCity,
                                  @RequestParam(value = "newState", required = false) String newState,
                                  @RequestParam(value = "newPincode", required = false) String newPincode,
                                  @RequestParam(value = "paymentMethod", defaultValue = "CREDIT_CARD") String paymentMethod,
                                  Model model,
                                  HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Cart> cartItems = cartService.getCartItems(currentUser);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        String shippingFormatted = "";
        if (addressId != null) {
            Optional<Address> addrOpt = addressService.getAddressById(addressId);
            if (addrOpt.isPresent()) {
                Address a = addrOpt.get();
                shippingFormatted = a.getFullAddress() + ", " + a.getCity() + ", " + (a.getState() != null ? a.getState() + " " : "") + a.getPincode();
            }
        }
        if (shippingFormatted.isBlank() && newFullAddress != null && !newFullAddress.isBlank()) {
            shippingFormatted = newFullAddress + ", " + newCity + ", " + (newState != null ? newState + " " : "") + newPincode;
            // Save address for future use
            addressService.saveAddress(currentUser, newFullAddress, newCity, newState, newPincode, false);
        }

        if (shippingFormatted.isBlank()) {
            return "redirect:/checkout/step1?error=address_required";
        }

        double subtotal = cartService.calculateSubtotal(cartItems);
        RewardPoint rewardPoint = rewardService.getRewardPoint(currentUser);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", Math.round(subtotal * 100.0) / 100.0);
        model.addAttribute("shippingAddress", shippingFormatted);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("availablePoints", rewardPoint.getTotalPoints());
        model.addAttribute("currentUser", currentUser);

        return "checkout-confirm";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("shippingAddress") String shippingAddress,
                             @RequestParam(value = "paymentMethod", defaultValue = "CREDIT_CARD") String paymentMethod,
                             @RequestParam(value = "pointsToRedeem", defaultValue = "0") Integer pointsToRedeem,
                             HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderService.createOrder(currentUser, shippingAddress, paymentMethod, pointsToRedeem);
        return "redirect:/checkout/success/" + order.getOrderId();
    }

    @GetMapping("/success/{orderId}")
    public String checkoutSuccess(@PathVariable("orderId") Integer orderId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }

        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("currentUser", currentUser);

        return "checkout-success";
    }
}
