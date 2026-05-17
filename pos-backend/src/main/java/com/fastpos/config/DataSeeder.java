package com.fastpos.config;

import com.fastpos.model.Category;
import com.fastpos.model.Product;
import com.fastpos.model.User;
import com.fastpos.repository.CategoryRepository;
import com.fastpos.repository.ProductRepository;
import com.fastpos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;
        log.info("Seeding initial data...");

        // Users
        userRepository.save(User.builder()
                .email("admin@fastpos.com").password(passwordEncoder.encode("admin123"))
                .firstName("Admin").lastName("User").phone("555-0100")
                .role(User.Role.ADMIN).active(true).build());

        userRepository.save(User.builder()
                .email("cashier@fastpos.com").password(passwordEncoder.encode("cashier123"))
                .firstName("Jane").lastName("Smith").phone("555-0101")
                .role(User.Role.CASHIER).active(true).build());

        userRepository.save(User.builder()
                .email("customer@fastpos.com").password(passwordEncoder.encode("customer123"))
                .firstName("John").lastName("Doe").phone("555-0102")
                .role(User.Role.CUSTOMER).active(true).build());

        // Categories
        Category beverages = categoryRepository.save(Category.builder().name("Beverages").description("Hot & cold drinks").build());
        Category food = categoryRepository.save(Category.builder().name("Food").description("Meals and snacks").build());
        Category desserts = categoryRepository.save(Category.builder().name("Desserts").description("Sweet treats").build());
        Category electronics = categoryRepository.save(Category.builder().name("Electronics").description("Tech gadgets").build());
        Category grocery = categoryRepository.save(Category.builder().name("Grocery").description("Daily essentials").build());

        // Products - Beverages
        productRepository.save(Product.builder().name("Espresso").description("Rich single-shot espresso").sku("BEV-001").price(new BigDecimal("3.50")).stockQuantity(200).category(beverages).imageUrl("https://images.unsplash.com/photo-1510707577719-ae7c14805e3a?w=300").active(true).build());
        productRepository.save(Product.builder().name("Cappuccino").description("Classic Italian cappuccino").sku("BEV-002").price(new BigDecimal("4.50")).stockQuantity(150).category(beverages).imageUrl("https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=300").active(true).build());
        productRepository.save(Product.builder().name("Latte").description("Smooth café latte").sku("BEV-003").price(new BigDecimal("4.75")).stockQuantity(180).category(beverages).imageUrl("https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=300").active(true).build());
        productRepository.save(Product.builder().name("Green Tea").description("Organic matcha green tea").sku("BEV-004").price(new BigDecimal("3.25")).stockQuantity(100).category(beverages).imageUrl("https://images.unsplash.com/photo-1556881286-fc6915169721?w=300").active(true).build());
        productRepository.save(Product.builder().name("Fresh Orange Juice").description("Freshly squeezed OJ").sku("BEV-005").price(new BigDecimal("5.00")).stockQuantity(80).category(beverages).imageUrl("https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=300").active(true).build());

        // Products - Food
        productRepository.save(Product.builder().name("Grilled Chicken Sandwich").description("Herb-grilled chicken on sourdough").sku("FOOD-001").price(new BigDecimal("8.99")).stockQuantity(50).category(food).imageUrl("https://images.unsplash.com/photo-1553909489-cd47e0907980?w=300").active(true).build());
        productRepository.save(Product.builder().name("Caesar Salad").description("Classic Caesar with croutons").sku("FOOD-002").price(new BigDecimal("7.50")).stockQuantity(40).category(food).imageUrl("https://images.unsplash.com/photo-1546793665-c74683f339c1?w=300").active(true).build());
        productRepository.save(Product.builder().name("Margherita Pizza").description("Wood-fired thin crust pizza").sku("FOOD-003").price(new BigDecimal("12.99")).stockQuantity(30).category(food).imageUrl("https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=300").active(true).build());
        productRepository.save(Product.builder().name("Fish & Chips").description("Beer-battered cod with fries").sku("FOOD-004").price(new BigDecimal("11.50")).stockQuantity(35).category(food).imageUrl("https://images.unsplash.com/photo-1580217593608-61931ceaa253?w=300").active(true).build());
        productRepository.save(Product.builder().name("Veggie Wrap").description("Mediterranean veggie wrap").sku("FOOD-005").price(new BigDecimal("7.99")).stockQuantity(45).category(food).imageUrl("https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=300").active(true).build());

        // Products - Desserts
        productRepository.save(Product.builder().name("Chocolate Brownie").description("Warm fudge brownie").sku("DES-001").price(new BigDecimal("4.99")).stockQuantity(60).category(desserts).imageUrl("https://images.unsplash.com/photo-1564355808539-22fda35bed7e?w=300").active(true).build());
        productRepository.save(Product.builder().name("Cheesecake").description("NY style cheesecake").sku("DES-002").price(new BigDecimal("6.50")).stockQuantity(25).category(desserts).imageUrl("https://images.unsplash.com/photo-1567171466295-4afa63d45416?w=300").active(true).build());
        productRepository.save(Product.builder().name("Tiramisu").description("Classic Italian tiramisu").sku("DES-003").price(new BigDecimal("7.25")).stockQuantity(20).category(desserts).imageUrl("https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=300").active(true).build());

        // Products - Electronics
        productRepository.save(Product.builder().name("Wireless Earbuds").description("Bluetooth 5.3 earbuds").sku("ELEC-001").price(new BigDecimal("29.99")).stockQuantity(100).category(electronics).imageUrl("https://images.unsplash.com/photo-1590658268037-6bf12f032f55?w=300").active(true).build());
        productRepository.save(Product.builder().name("USB-C Hub").description("7-in-1 USB-C hub").sku("ELEC-002").price(new BigDecimal("24.99")).stockQuantity(75).category(electronics).imageUrl("https://images.unsplash.com/photo-1625842268584-8f3296236761?w=300").active(true).build());
        productRepository.save(Product.builder().name("Phone Charger").description("Fast 65W GaN charger").sku("ELEC-003").price(new BigDecimal("19.99")).stockQuantity(120).category(electronics).imageUrl("https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=300").active(true).build());

        // Products - Grocery
        productRepository.save(Product.builder().name("Organic Milk").description("1L organic whole milk").sku("GRO-001").price(new BigDecimal("3.99")).stockQuantity(50).category(grocery).imageUrl("https://images.unsplash.com/photo-1563636619-e9143da7973b?w=300").active(true).build());
        productRepository.save(Product.builder().name("Sourdough Bread").description("Artisan sourdough loaf").sku("GRO-002").price(new BigDecimal("5.49")).stockQuantity(40).category(grocery).imageUrl("https://images.unsplash.com/photo-1549931319-a545753467c8?w=300").active(true).build());
        productRepository.save(Product.builder().name("Eggs (Dozen)").description("Free-range eggs").sku("GRO-003").price(new BigDecimal("4.99")).stockQuantity(60).category(grocery).imageUrl("https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=300").active(true).build());
        productRepository.save(Product.builder().name("Avocado (3-pack)").description("Ripe Hass avocados").sku("GRO-004").price(new BigDecimal("4.50")).stockQuantity(45).category(grocery).imageUrl("https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=300").active(true).build());

        log.info("Data seeding complete: 3 users, 5 categories, 20 products");
    }
}
