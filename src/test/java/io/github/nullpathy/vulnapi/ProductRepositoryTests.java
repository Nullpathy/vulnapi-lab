package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Product;
import io.github.nullpathy.vulnapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProductById() {
        Product product = new Product(
                "Mechanical Keyboard",
                "Mechanical keyboard for testing purposes",
                new BigDecimal("89.90"),
                10
        );

        Product savedProduct = productRepository.save(product);

        var result = productRepository.findById(savedProduct.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Mechanical Keyboard");
        assertThat(result.get().getPrice())
                .isEqualByComparingTo(new BigDecimal("89.90"));
        assertThat(result.get().getStock()).isEqualTo(10);
    }
}