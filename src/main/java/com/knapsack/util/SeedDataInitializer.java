package com.knapsack.util;

import com.knapsack.model.*;
import com.knapsack.repository.*;
import com.knapsack.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SeedDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ReviewRepository reviewRepository;
    private final RewardService rewardService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SeedDataInitializer(CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository,
                               AddressRepository addressRepository,
                               ReviewRepository reviewRepository,
                               RewardService rewardService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.reviewRepository = reviewRepository;
        this.rewardService = rewardService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedCategoriesAndProducts();
        ensure1000DemoProducts();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            User admin = new User(
                    null,
                    "Admin User",
                    "admin@knapsack.com",
                    "+1-800-555-0199",
                    passwordEncoder.encode("admin123"),
                    "ADMIN"
            );
            userRepository.save(admin);

            User customer = new User(
                    null,
                    "John Doe",
                    "john@example.com",
                    "+1-800-555-0100",
                    passwordEncoder.encode("user123"),
                    "CUSTOMER"
            );
            customer = userRepository.save(customer);

            User demoUser = new User(
                    null,
                    "Demo Customer",
                    "user@knapsack.com",
                    "+1-800-555-0101",
                    passwordEncoder.encode("user123"),
                    "CUSTOMER"
            );
            userRepository.save(demoUser);

            // Add default saved address for John Doe
            Address defaultAddress = new Address(
                    customer,
                    "123 Innovation Way, Suite 400",
                    "San Francisco",
                    "CA",
                    "94107",
                    true
            );
            addressRepository.save(defaultAddress);

            // Process initial reward points (+50 welcome bonus)
            rewardService.checkAndProcessDailyLoginReward(customer);
            rewardService.checkAndProcessDailyLoginReward(demoUser);
        }
    }

    private void seedCategoriesAndProducts() {
        if (categoryRepository.count() == 0) {
            Category backpacks = new Category("Backpacks & Travel", "Premium ergonomic backpacks, duffels, and travel gear", "fa-bag-shopping", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80");
            Category electronics = new Category("Electronics", "Smart gadgets, audio gear, and cutting-edge devices", "fa-laptop", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80");
            Category fashion = new Category("Fashion & Apparel", "Modern streetwear, outdoor jackets, and comfortable activewear", "fa-shirt", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?auto=format&fit=crop&w=600&q=80");
            Category accessories = new Category("Accessories", "Watches, sunglasses, wallets, and tech accessories", "fa-glasses", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80");

            categoryRepository.saveAll(Arrays.asList(backpacks, electronics, fashion, accessories));

            List<Product> products = Arrays.asList(
                    new Product("Nomad Pro Tactical Backpack 35L (Demo Product - 1000 Stock)", "Water-resistant, TSA-friendly laptop compartment, ergonomic mesh padding, and hidden anti-theft pocket.", 9999.00, 7999.00, 1000, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=800&q=80", backpacks, true),
                    new Product("Urban Transit Minimalist Daypack", "Sleek matte finish waterproof fabric, 15-inch padded sleeve, integrated USB charging port.", 6999.00, 4999.00, 1000, "https://images.unsplash.com/photo-1622560480605-d83c853bc5c3?auto=format&fit=crop&w=800&q=80", backpacks, true),
                    new Product("Aura Active ANC Wireless Headphones", "Hybrid active noise cancellation, 40-hour battery life, spatial audio drivers, and memory foam earcups.", 18999.00, 14999.00, 1000, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80", electronics, true),
                    new Product("PulseFit Pro Smartwatch Ultra", "AMOLED HD display, ECG & SpO2 sensors, multi-sport GPS tracking, titanium bezel.", 14999.00, 11999.00, 1000, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80", electronics, true),
                    new Product("All-Weather Waterproof Shell Jacket", "Breathable GORE-TEX fabric, sealed seams, adjustable hood, storm flap pockets.", 12999.00, 9999.00, 1000, "https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80", fashion, true),
                    new Product("RFID Blocking Leather Minimalist Wallet", "Top-grain Italian leather, quick-access card ejection button, slim profile.", 2999.00, 1999.00, 1000, "https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=800&q=80", accessories, true)
            );

            productRepository.saveAll(products);

            // Seed initial sample review for the first product
            userRepository.findByEmail("john@example.com").ifPresent(customer -> {
                Product p1 = products.get(0);
                Review review = new Review(customer, p1, 5, "Outstanding build quality! Traveled across Europe with this bag, highly recommended.");
                reviewRepository.save(review);
                p1.setRating(5.0);
                p1.setReviewCount(1);
                productRepository.save(p1);
            });
        }
    }

    private void ensure1000DemoProducts() {
        // If products exist with old USD pricing (< 1000), update or re-seed them
        List<Product> existing = productRepository.findAll();
        if (!existing.isEmpty() && existing.get(0).getPrice() < 500) {
            for (Product p : existing) {
                p.setPrice(Math.round(p.getPrice() * 85.0 * 100.0) / 100.0);
                if (p.getDiscountPrice() != null) {
                    p.setDiscountPrice(Math.round(p.getDiscountPrice() * 85.0 * 100.0) / 100.0);
                }
            }
            productRepository.saveAll(existing);
        }

        long currentCount = productRepository.count();
        if (currentCount >= 1000) return;

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) return;

        String[] adjectives = {"Nomad Pro", "Apex Tactical", "Vanguard Stealth", "Titanium Ultra", "Summit Explorer", "Urban Commuter", "Polaris Elite", "Aero Flight", "Cipher Shield", "Maverick Heavy-Duty", "Quantum Matrix", "Eclipse Modular", "Hyperion Core", "Starlight Edition", "Horizon Travel", "Zenith Peak"};
        
        String[][] nounsPerCat = {
            {"35L Backpack", "Expandable Travel Duffel", "Commuter Laptop Sling", "Roll-Top Waterproof Bag", "Modular EDC Pouch", "Tactical Carry-On Pack", "Minimalist Daypack", "Hiking Rucksack", "Anti-Theft Camera Bag", "Weekender Carryall"},
            {"ANC Wireless Headphones", "Pro Smartwatch", "Portable Power Bank 20000mAh", "Bluetooth Travel Speaker", "Wireless Earbuds Ultra", "MagSafe Car Mount Charger", "USB-C Multiport Dock", "Noise-Isolating Earplugs", "Smart Fitness Tracker", "High-Speed Action Camera"},
            {"All-Weather Shell Jacket", "Tactical Cargo Pants", "Breathable Travel Hoodie", "Thermal Fleece Midlayer", "Waterproof Outdoor Parka", "Performance Running Tee", "Windproof Softshell Vest", "Merino Wool Travel Socks", "UV Protection Sun Shirt", "Insulated Winter Gloves"},
            {"RFID Blocking Leather Wallet", "Titanium Field Watch", "Polarized Aviator Sunglasses", "Stainless Steel Water Bottle", "Paracord Survival Bracelet", "Leather Travel Passport Case", "Key-Organizer Carabiner", "EDC Pocket Multi-Tool", "Compact Travel Umbrella", "Tactical Pen & Pointer"}
        };

        String[][] imagesPerCat = {
            {"https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1622560480605-d83c853bc5c3?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1546938576-6e6a64f317cc?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1581605405669-fcdf81165afa?auto=format&fit=crop&w=800&q=80"},
            {"https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=800&q=80"},
            {"https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=800&q=80"},
            {"https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1572635196237-14b3f281503f?auto=format&fit=crop&w=800&q=80", "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?auto=format&fit=crop&w=800&q=80"}
        };

        long needed = 1000 - currentCount;
        List<Product> newProducts = new java.util.ArrayList<>();

        for (int i = 1; i <= needed; i++) {
            int catIdx = (i - 1) % categories.size();
            Category category = categories.get(catIdx);
            
            int catNounsIdx = Math.min(catIdx, nounsPerCat.length - 1);
            int adjIdx = (i / categories.size()) % adjectives.length;
            int nounIdx = (i / (categories.size() * adjectives.length)) % nounsPerCat[catNounsIdx].length;
            int imgIdx = i % imagesPerCat[catNounsIdx].length;

            String pName = adjectives[adjIdx] + " " + nounsPerCat[catNounsIdx][nounIdx] + " #" + (currentCount + i);
            String desc = "High performance " + category.getName() + " engineered for extreme durability, daily commute, and travel convenience. Model #" + (1000 + i);
            double basePrice = Math.round((1499.0 + (i % 450) * 85.0) * 100.0) / 100.0;
            double discountPrice = (i % 3 == 0) ? Math.round((basePrice * 0.85) * 100.0) / 100.0 : basePrice;
            int stock = 1000; // 1000 units available for purchase
            boolean featured = (i % 20 == 0);
            String imgUrl = imagesPerCat[catNounsIdx][imgIdx];

            Product p = new Product(pName, desc, basePrice, discountPrice, stock, imgUrl, category, featured);
            p.setRating(Math.round((4.0 + (i % 10) * 0.1) * 10.0) / 10.0);
            p.setReviewCount(5 + (i % 45));
            newProducts.add(p);

            if (newProducts.size() >= 200) {
                productRepository.saveAll(newProducts);
                newProducts.clear();
            }
        }

        if (!newProducts.isEmpty()) {
            productRepository.saveAll(newProducts);
        }
    }
}
