package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Order;
import io.github.nullpathy.vulnapi.entity.Product;
import io.github.nullpathy.vulnapi.entity.Role;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.repository.OrderRepository;
import io.github.nullpathy.vulnapi.repository.ProductRepository;
import io.github.nullpathy.vulnapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryTests {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveOrderWithUserAndProduct() {
        User user = userRepository.save(
                new User(
                        "Order Test User",
                        "order-test@vulnapi.local",
                        "temporary-password",
                        Role.USER
                )
        );

        Product product = productRepository.save(
                new Product(
                        "Test Product",
                        "Product used for order testing",
                        new BigDecimal("99.90"),
                        10
                )
        );

        Order order = orderRepository.save(
                new Order(user, product, 2)
        );

        var result = orderRepository.findById(order.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getProduct().getId()).isEqualTo(product.getId());
        assertThat(result.get().getQuantity()).isEqualTo(2);
        assertThat(result.get().getCreatedAt()).isNotNull();
    }
}