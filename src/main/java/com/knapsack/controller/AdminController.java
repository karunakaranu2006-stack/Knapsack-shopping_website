package com.knapsack.controller;

import com.knapsack.model.Category;
import com.knapsack.model.Order;
import com.knapsack.model.Product;
import com.knapsack.model.User;
import com.knapsack.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final AdminReportService adminReportService;

    @Autowired
    public AdminController(ProductService productService,
                           CategoryService categoryService,
                           OrderService orderService,
                           UserService userService,
                           ReviewService reviewService,
                           AdminReportService adminReportService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.reviewService = reviewService;
        this.adminReportService = adminReportService;
    }

    private boolean checkAdmin(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }

        List<Product> products = productService.getAllProducts();
        List<Order> orders = orderService.getAllOrders();
        double totalRevenue = orders.stream()
                .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
                .mapToDouble(Order::getFinalAmount)
                .sum();

        model.addAttribute("productCount", products.size());
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        model.addAttribute("products", products.stream().limit(6).toList());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "admin/dashboard";
    }

    // User Management
    @GetMapping("/users")
    public String listUsers(Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/users";
    }

    @GetMapping("/users/block/{id}")
    public String blockUser(@PathVariable("id") Integer id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";
        userService.blockUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/unblock/{id}")
    public String unblockUser(@PathVariable("id") Integer id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";
        userService.unblockUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // Category Management
    @GetMapping("/categories")
    public String listCategories(Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";
        categoryService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    // Product Management
    @GetMapping("/products")
    public String listProducts(Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Integer id, Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) return "redirect:/admin/products";

        model.addAttribute("product", productOpt.get());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("categoryId") Integer categoryId,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                product.setImageUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        categoryService.getCategoryById(categoryId).ifPresent(product::setCategory);
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    // Order Management
    @GetMapping("/orders")
    public String listOrders(Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/orders";
    }

    @PostMapping("/orders/status")
    public String updateOrderStatus(@RequestParam("orderId") Integer orderId,
                                    @RequestParam("status") String status,
                                    HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders";
    }

    // Review Management
    @GetMapping("/reviews")
    public String listReviews(Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        model.addAttribute("reviews", reviewService.getAllReviews());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/reviews";
    }

    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable("id") Integer id, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";
        reviewService.deleteReview(id);
        return "redirect:/admin/reviews";
    }

    // Analytical Reports
    @GetMapping("/reports")
    public String viewReports(Model model, HttpSession session) {
        if (!checkAdmin(session)) return "redirect:/login";

        model.addAttribute("report", adminReportService.generateAnalyticalReport());
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(10).toList());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "admin/reports";
    }
}
