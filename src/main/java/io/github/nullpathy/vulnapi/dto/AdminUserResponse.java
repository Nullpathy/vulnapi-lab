package io.github.nullpathy.vulnapi.dto;

import io.github.nullpathy.vulnapi.entity.Role;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String password
) {
}