package io.github.nullpathy.vulnapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 500)
        String description,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal price,

        @NotNull
        @Min(0)
        Integer stock

) {
}