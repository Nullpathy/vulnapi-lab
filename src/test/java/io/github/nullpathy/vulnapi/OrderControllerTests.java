package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Product;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.security.JwtService;
import io.github.nullpathy.vulnapi.service.ProductService;
import io.github.nullpathy.vulnapi.service.UserService;
import io.github.nullpathy.vulnapi.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Test
    void shouldRejectUnauthenticatedAccessToOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void shouldRejectUnauthenticatedAccessToOrderById() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedOrderCreation() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidJwtToken() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void shouldAllowAuthenticatedAccessWithValidJwt() throws Exception {

        User user = userService.createUser(
                "JWT Order Test User",
                "jwt-order-test@vulnapi.local",
                "SecurePassword123"
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/orders")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        ))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateOrderForAuthenticatedUser() throws Exception {

        User user = userService.createUser(
                "Order Owner",
                "order-owner@vulnapi.local",
                "SecurePassword123"
        );

        Product product = productService.createProduct(
                "Order Test Product",
                "Product used to verify JWT ownership",
                new BigDecimal("79.99"),
                10
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                            {
                              "productId": %d,
                              "quantity": 2
                            }
                            """.formatted(product.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void shouldReturnOnlyOrdersOwnedByAuthenticatedUser() throws Exception {

        User alice = userService.createUser(
                "Alice",
                "alice-orders@vulnapi.local",
                "SecurePassword123"
        );

        User bob = userService.createUser(
                "Bob",
                "bob-orders@vulnapi.local",
                "SecurePassword123"
        );

        Product product = productService.createProduct(
                "Shared Product",
                "Product used for order ownership testing",
                new BigDecimal("49.90"),
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

        String aliceToken = jwtService.generateToken(alice);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(alice.getId()))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    void shouldRejectAccessToAnotherUsersOrder() throws Exception {

        User alice = userService.createUser(
                "Alice",
                "alice-bola-test@vulnapi.local",
                "SecurePassword123"
        );

        User bob = userService.createUser(
                "Bob",
                "bob-bola-test@vulnapi.local",
                "SecurePassword123"
        );

        Product product = productService.createProduct(
                "BOLA Test Product",
                "Product used for ownership authorization testing",
                new BigDecimal("99.90"),
                10
        );

        var bobOrder = orderService.createOrder(
                bob.getEmail(),
                product.getId(),
                1
        );

        String aliceToken = jwtService.generateToken(alice);

        mockMvc.perform(get("/api/orders/" + bobOrder.getId())
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowAccessToOwnOrder() throws Exception {

        User user = userService.createUser(
                "Order Owner",
                "own-order-test@vulnapi.local",
                "SecurePassword123"
        );

        Product product = productService.createProduct(
                "Own Order Product",
                "Product used for ownership authorization testing",
                new BigDecimal("89.90"),
                10
        );

        var order = orderService.createOrder(
                user.getEmail(),
                product.getId(),
                2
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/orders/" + order.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.quantity").value(2));
    }
}