package com.ecommerce.config;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent startup seeding:
 *  - the three storefront categories (tshirts, shirts, pants);
 *  - 12 sample products (4 per category), only if they do not yet exist by name;
 *  - the first admin, only if no admin exists AND ADMIN_PASSWORD is provided.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    public DataInitializer(CategoryRepository categoryRepository, UserRepository userRepository,
                           ProductRepository productRepository, PasswordEncoder passwordEncoder,
                           AppProperties props) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedCategories();
        seedProducts();
        seedAdmin();
    }

    private void seedCategories() {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("tshirts", "T-Shirts");
        defaults.put("shirts", "Shirts");
        defaults.put("pants", "Pants");
        defaults.forEach((slug, name) -> {
            if (!categoryRepository.existsBySlug(slug)) {
                Category c = new Category();
                c.setSlug(slug);
                c.setName(name);
                categoryRepository.save(c);
                log.info("Created category '{}'", slug);
            }
        });
    }

    /**
     * Seeds sample products. Checks by name so it is safe to run on every startup.
     */
    private void seedProducts() {
        // {slug, name, description, price, imageUrl}
        List<Object[]> samples = List.of(
            new Object[]{"shirts", "Classic Green Linen Shirt",
                "Lightweight linen blend, relaxed collar, perfect for casual Fridays.",
                1299.00, "/shirt1.webp"},
            new Object[]{"shirts", "Navy Blue Formal Shirt",
                "100% cotton poplin, slim fit, ideal for office and evening wear.",
                1499.00, "/shirt2.webp"},
            new Object[]{"shirts", "Ivory Patterned Shirt",
                "Subtle geometric print on premium cotton, pairs with chinos or jeans.",
                1199.00, "/shirt3.webp"},
            new Object[]{"shirts", "Charcoal Oxford Shirt",
                "Classic Oxford weave in charcoal grey, versatile, timeless, durable.",
                1399.00, "/shirt4.webp"},
            new Object[]{"tshirts", "Olive Green Drop-Shoulder Tee",
                "Oversized drop-shoulder cut in 220 GSM combed cotton. Stonewash finish.",
                799.00, "/tshirt1.webp"},
            new Object[]{"tshirts", "Black Striped Graphic Tee",
                "Bold stripe pattern screen-printed on breathable jersey cotton.",
                599.00, "/tshirt2.webp"},
            new Object[]{"tshirts", "Cherry Red Classic Tee",
                "Crew neck, slim fit, preshrunk, a wardrobe essential in rich red.",
                699.00, "/tshirt3.webp"},
            new Object[]{"tshirts", "Cloud White Essential Tee",
                "Ultra-soft Pima cotton. Minimal, clean, everyday-ready.",
                499.00, "/tshirt4.webp"},
            new Object[]{"pants", "Beige Wide-Leg Pant",
                "High-rise wide-leg silhouette in linen blend. Effortlessly stylish.",
                2499.00, "/pant1.jpg"},
            new Object[]{"pants", "Brown Slim Trousers",
                "Italian-cut slim trousers in mid-weight wool blend. Tapered hem.",
                1899.00, "/pant2.jpg"},
            new Object[]{"pants", "Dusty Pink Relaxed Pant",
                "Relaxed-fit jogger-inspired pant with drawstring waist. Weekend vibes.",
                1599.00, "/pant3.jpg"},
            new Object[]{"pants", "Slate Grey Chino",
                "Classic chino in slate grey, structured enough for work, casual for anywhere.",
                1749.00, "/pant4.jpg"}
        );

        // Load all existing product names once to avoid N+1 existence checks.
        java.util.Set<String> existing = new java.util.HashSet<>();
        productRepository.findAll().forEach(p -> existing.add(p.getName().toLowerCase(Locale.ROOT)));

        for (Object[] row : samples) {
            String slug        = (String)  row[0];
            String name        = (String)  row[1];
            String description = (String)  row[2];
            double price       = (double)  row[3];
            String imageUrl    = (String)  row[4];

            if (existing.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }

            Category category = categoryRepository.findBySlug(slug).orElse(null);
            if (category == null) {
                log.warn("Category '{}' not found; skipping product '{}'", slug, name);
                continue;
            }

            Product p = new Product();
            p.setName(name);
            p.setDescription(description);
            p.setPrice(BigDecimal.valueOf(price));
            p.setStockQuantity(50);
            p.setImageUrl(imageUrl);
            p.setCategory(category);
            productRepository.save(p);
            existing.add(name.toLowerCase(Locale.ROOT));
            log.info("Seeded product '{}' -> category '{}'", name, slug);
        }
    }

    private void seedAdmin() {
        if (userRepository.countByRole(Role.ADMIN) > 0) {
            return;
        }
        AppProperties.Admin admin = props.admin();
        if (admin == null || admin.password() == null || admin.password().isBlank()) {
            log.warn("No admin account exists and ADMIN_PASSWORD is not set. Set ADMIN_EMAIL / ADMIN_PASSWORD "
                    + "and restart to create the first admin.");
            return;
        }
        if (admin.password().length() < 8) {
            log.warn("ADMIN_PASSWORD must be at least 8 characters. Admin was NOT created.");
            return;
        }
        User user = new User();
        user.setFullName(admin.fullName() == null || admin.fullName().isBlank() ? "Administrator" : admin.fullName());
        user.setEmail(admin.email().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(admin.password()));
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        log.info("Created first admin account {}", user.getEmail());
    }
}