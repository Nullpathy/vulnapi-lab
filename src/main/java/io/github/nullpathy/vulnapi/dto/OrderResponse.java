package io.github.nullpathy.vulnapi.dto;

import java.time.LocalDateTime;

public record OrderResponse(

        Long id,
        Long userId,
        Long productId,
        Integer quantity,
        LocalDateTime createdAt

) {}