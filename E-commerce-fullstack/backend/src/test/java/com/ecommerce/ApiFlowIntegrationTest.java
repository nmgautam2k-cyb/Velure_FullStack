package com.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** End-to-end API flow on an in-memory H2 database (MySQL mode). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    private Long productId;

    @BeforeEach
    void seedProduct() {
        Category shirts = categoryRepository.findBySlug("shirts").orElseThrow();
        Product p = new Product();
        p.setName("Test Shirt " + UUID.randomUUID());
        p.setPrice(new BigDecimal("1000.00"));
        p.setStockQuantity(5);
        p.setCategory(shirts);
        productId = productRepository.save(p).getId();
    }

    private String body(Object o) throws Exception {
        return json.writeValueAsString(o);
    }

    private String register(String email) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "Test User", "email", email, "password", "Passw0rd!x"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").value("customer"));
        return login(email, "Passw0rd!x");
    }

    private String login(String email, String password) throws Exception {
        MvcResult res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return json.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void registrationRejectsDuplicatesWeakPasswordsAndIgnoresRoleEscalation() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "A", "email", email, "password", "Passw0rd!x", "role", "admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("customer")); // role from the request is ignored
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "A", "email", email, "password", "Passw0rd!x"))))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "A", "email", "x@y.com", "password", "123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongPasswordAndRoleMismatchAreUnauthorized() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@test.com";
        register(email);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", "Passw0rd!x", "role", "admin"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireAuthAndAdminEndpointsRequireAdmin() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
        String userToken = register("plain-" + UUID.randomUUID() + "@test.com");
        mvc.perform(get("/api/admin/orders").header("Authorization", bearer(userToken))).andExpect(status().isForbidden());
        mvc.perform(post("/api/products").header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("title", "x", "price", 10, "stockQuantity", 1, "category", "shirts"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/products")).andExpect(status().isOk()); // public
    }

    @Test
    void checkoutDecrementsStockUsesServerPricesAndIsolatesUsers() throws Exception {
        String alice = register("alice-" + UUID.randomUUID() + "@test.com");
        String bob = register("bob-" + UUID.randomUUID() + "@test.com");

        // add 2 units to Alice's cart; total is computed by the server
        mvc.perform(post("/api/cart/items").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("productId", productId, "quantity", 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(2000.0));

        // more than the stock is refused
        mvc.perform(post("/api/cart/items").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("productId", productId, "quantity", 4))))
                .andExpect(status().isBadRequest());

        // address too short -> validation error
        mvc.perform(post("/api/orders").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("shippingAddress", "x", "paymentMethod", "COD"))))
                .andExpect(status().isBadRequest());

        MvcResult created = mvc.perform(post("/api/orders").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("shippingAddress", "12 Main Street, Chennai 600001", "paymentMethod", "COD"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(2000.0))
                .andReturn();
        JsonNode order = json.readTree(created.getResponse().getContentAsString());
        long orderId = order.get("id").asLong();

        assertThat(productRepository.findById(productId).orElseThrow().getStockQuantity()).isEqualTo(3);

        // cart emptied after the order
        mvc.perform(get("/api/cart").header("Authorization", bearer(alice)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(0));

        // Alice sees her order; Bob cannot fetch it by id (404, no leakage) and has none of his own
        mvc.perform(get("/api/orders/my-orders").header("Authorization", bearer(alice)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
        mvc.perform(get("/api/orders/" + orderId).header("Authorization", bearer(bob)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/my-orders").header("Authorization", bearer(bob)))
                .andExpect(jsonPath("$.content.length()").value(0));

        // empty cart cannot be checked out
        mvc.perform(post("/api/orders").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("shippingAddress", "12 Main Street, Chennai 600001", "paymentMethod", "COD"))))
                .andExpect(status().isBadRequest());

        // admin cancels -> stock is restored
        String admin = login("admin@test.local", "AdminPass123");
        mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("status", "cancelled"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("cancelled"));
        assertThat(productRepository.findById(productId).orElseThrow().getStockQuantity()).isEqualTo(5);

        // a product that appears in orders cannot be deleted
        mvc.perform(delete("/api/products/" + productId).header("Authorization", bearer(admin)))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanCreateProductButNotWithInlineImageOrNegativePrice() throws Exception {
        String admin = login("admin@test.local", "AdminPass123");
        mvc.perform(post("/api/products").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("title", "Admin Pant", "price", 1999.5, "stockQuantity", 10,
                                "category", "pants", "image", "/pant1.jpg"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("pants"));
        mvc.perform(post("/api/products").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("title", "Bad", "price", -5, "stockQuantity", 1, "category", "pants"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/products").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("title", "Bad", "price", 5, "stockQuantity", -1, "category", "pants"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/products").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("title", "Bad", "price", 5, "stockQuantity", 1, "category", "pants",
                                "image", "data:image/png;base64,AAAA"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wishlistIsPerUserAndIdempotent() throws Exception {
        String token = register("wish-" + UUID.randomUUID() + "@test.com");
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/wishlist/" + productId).header("Authorization", bearer(token)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.length()").value(1));
        }
        mvc.perform(delete("/api/wishlist/" + productId).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
}
