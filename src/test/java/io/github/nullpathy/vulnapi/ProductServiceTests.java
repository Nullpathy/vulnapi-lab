package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Product;
import io.github.nullpathy.vulnapi.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProductServiceTests {

    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        Product product = productService.createProduct(
                "Security Key",
                "Hardware security key",
                new BigDecimal("49.90"),
                20
        );

        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo("Security Key");
        assertThat(product.getPrice())
                .isEqualByComparingTo(new BigDecimal("49.90"));
        assertThat(product.getStock()).isEqualTo(20);
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThatThrownBy(() ->
                productService.createProduct(
                        "Invalid Product",
                        "Product with invalid price",
                        new BigDecimal("-10.00"),
                        5
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price cannot be negative");
    }

    @Test
    void shouldRejectNegativeStock() {
        assertThatThrownBy(() ->
                productService.createProduct(
                        "Invalid Product",
                        "Product with invalid stock",
                        new BigDecimal("10.00"),
                        -1
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stock cannot be negative");
    }
}