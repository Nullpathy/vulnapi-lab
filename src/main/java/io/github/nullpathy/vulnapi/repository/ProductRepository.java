package io.github.nullpathy.vulnapi.repository;

import io.github.nullpathy.vulnapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}