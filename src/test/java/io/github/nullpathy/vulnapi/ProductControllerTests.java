package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Product;
import io.github.nullpathy.vulnapi.entity.Role;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.repository.UserRepository;
import io.github.nullpathy.vulnapi.security.JwtService;
import io.github.nullpathy.vulnapi.service.ProductService;
import io.github.nullpathy.vulnapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductService productService;

    @Test
    void shouldAllowPublicAccessToProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldRejectProductCreationForUserRole() throws Exception {

        User user = userService.createUser(
                "Normal User",
                "product-user@vulnapi.local",
                "SecurePassword123"
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Test Product",
                                  "description": "Security test product",
                                  "price": 99.99,
                                  "stock": 10
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowProductCreationForAdminRole() throws Exception {

        User admin = userService.createUser(
                "Admin User",
                "product-admin@vulnapi.local",
                "SecurePassword123"
        );

        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        String token = jwtService.generateToken(admin);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Admin Product",
                                  "description": "Created by an administrator",
                                  "price": 149.99,
                                  "stock": 20
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectProductDeletionWithoutAuthentication() throws Exception {

        Product product = productService.createProduct(
                "Delete Test Product",
                "Product for unauthenticated deletion test",
                new BigDecimal("49.99"),
                5
        );

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProductDeletionForUserRole() throws Exception {

        Product product = productService.createProduct(
                "User Delete Test",
                "Product that USER must not delete",
                new BigDecimal("59.99"),
                5
        );

        User user = userService.createUser(
                "Delete Normal User",
                "delete-user@vulnapi.local",
                "SecurePassword123"
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(delete("/api/products/{id}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowProductDeletionForAdminRole() throws Exception {

        Product product = productService.createProduct(
                "Admin Delete Test",
                "Product that ADMIN can delete",
                new BigDecimal("69.99"),
                5
        );

        User admin = userService.createUser(
                "Delete Admin",
                "delete-admin@vulnapi.local",
                "SecurePassword123"
        );

        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        String token = jwtService.generateToken(admin);

        mockMvc.perform(delete("/api/products/{id}", product.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}