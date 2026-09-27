package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Product;
import io.github.nullpathy.vulnapi.entity.Role;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.repository.UserRepository;
import io.github.nullpathy.vulnapi.security.JwtService;
import io.github.nullpathy.vulnapi.service.OrderService;
import io.github.nullpathy.vulnapi.service.ProductService;
import io.github.nullpathy.vulnapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminOrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRejectUnauthenticatedAccessToAdminOrders() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRegularUserAccessToAdminOrders() throws Exception {
        User user = userService.createUser(
                "Regular User",
                "regular-admin-order-test@vulnapi.local",
                "SecurePassword123"
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToViewAllOrders() throws Exception {
        User alice = userService.createUser(
                "Alice",
                "alice-admin-order-test@vulnapi.local",
                "SecurePassword123"
        );

        User bob = userService.createUser(
                "Bob",
                "bob-admin-order-test@vulnapi.local",
                "SecurePassword123"
        );

        User admin = userService.createUser(
                "Admin",
                "admin-order-test@vulnapi.local",
                "SecurePassword123"
        );

        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        Product product = productService.createProduct(
                "Admin Order Product",
                "Product used for admin order testing",
                new BigDecimal("129.90"),
                10
        );

        orderService.createOrder(
                alice.getEmail(),
                product.getId(),
                2
        );

        orderService.createOrder(
                bob.getEmail(),
                product.getId(),
                1
        );

        String adminToken = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[1].userId").exists());
    }
}