package com.knapsack.controller;

import com.knapsack.model.User;
import com.knapsack.service.RewardService;
import com.knapsack.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;
    private final RewardService rewardService;

    @Autowired
    public AuthController(UserService userService, RewardService rewardService) {
        this.userService = userService;
        this.rewardService = rewardService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "redirect", required = false) String redirect,
                                @RequestParam(value = "error", required = false) String error,
                                Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("redirect", redirect);
        if ("account_blocked".equals(error)) {
            model.addAttribute("errorMessage", "Your account has been suspended. Please contact support.");
        } else if (error != null) {
            model.addAttribute("errorMessage", "Invalid email address or password.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam(value = "redirect", required = false) String redirect,
                               HttpSession session,
                               Model model) {
        Optional<User> userOpt = userService.authenticate(email, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getIsBlocked()) {
                model.addAttribute("errorMessage", "Your account has been suspended by an administrator.");
                return "login";
            }

            session.setAttribute("currentUser", user);
            
            // Trigger Daily Login Reward Check (+5 points)
            rewardService.checkAndProcessDailyLoginReward(user);

            if (redirect != null && !redirect.isBlank()) {
                return "redirect:" + redirect;
            }
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin";
            }
            return "redirect:/";
        } else {
            model.addAttribute("errorMessage", "Invalid email address or password.");
            model.addAttribute("email", email);
            model.addAttribute("redirect", redirect);
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam("fullName") String fullName,
                                      @RequestParam("email") String email,
                                      @RequestParam("phone") String phone,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      HttpSession session,
                                      Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "register";
        }

        try {
            User user = userService.registerUser(fullName, email, phone, password, "CUSTOMER");
            session.setAttribute("currentUser", user);
            rewardService.checkAndProcessDailyLoginReward(user);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            model.addAttribute("successMessage", "Password reset instructions have been sent to " + email);
        } else {
            model.addAttribute("errorMessage", "No account found with email address: " + email);
        }
        return "forgot-password";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(HttpSession session) {
        if (session.getAttribute("currentUser") == null) {
            return "redirect:/login";
        }
        return "change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam("currentPassword") String currentPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        HttpSession session,
                                        Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "New passwords do not match.");
            return "change-password";
        }

        boolean success = userService.changePassword(currentUser, currentPassword, newPassword);
        if (success) {
            model.addAttribute("successMessage", "Your password has been changed successfully.");
        } else {
            model.addAttribute("errorMessage", "Incorrect current password.");
        }
        return "change-password";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
