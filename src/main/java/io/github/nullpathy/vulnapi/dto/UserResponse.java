package io.github.nullpathy.vulnapi.dto;

import io.github.nullpathy.vulnapi.entity.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {
}