package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Order;
import io.github.nullpathy.vulnapi.entity.Product;
import io.github.nullpathy.vulnapi.entity.Role;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.repository.ProductRepository;
import io.github.nullpathy.vulnapi.repository.UserRepository;
import io.github.nullpathy.vulnapi.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderServiceTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldCreateOrderAndReduceStock() {
        User user = createTestUser();

        Product product = createTestProduct(10);

        Order order = orderService.createOrder(
                user.getId(),
                product.getId(),
                3
        );

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertThat(order.getId()).isNotNull();
        assertThat(order.getUser().getId()).isEqualTo(user.getId());
        assertThat(order.getProduct().getId()).isEqualTo(product.getId());
        assertThat(order.getQuantity()).isEqualTo(3);

        assertThat(updatedProduct.getStock()).isEqualTo(7);
    }

    @Test
    void shouldRejectOrderWhenStockIsInsufficient() {
        User user = createTestUser();

        Product product = createTestProduct(2);

        assertThatThrownBy(() ->
                orderService.createOrder(
                        user.getId(),
                        product.getId(),
                        3
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient stock");

        Product unchangedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertThat(unchangedProduct.getStock()).isEqualTo(2);
    }

    @Test
    void shouldRejectInvalidQuantity() {
        User user = createTestUser();

        Product product = createTestProduct(10);

        assertThatThrownBy(() ->
                orderService.createOrder(
                        user.getId(),
                        product.getId(),
                        0
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    private User createTestUser() {
        return userRepository.save(
                new User(
                        "Order Service User",
                        "order-service-test@vulnapi.local",
                        "temporary-password",
                        Role.USER
                )
        );
    }

    private Product createTestProduct(Integer stock) {
        return productRepository.save(
                new Product(
                        "Order Service Product",
                        "Product used for order service testing",
                        new BigDecimal("59.90"),
                        stock
                )
        );
    }
}