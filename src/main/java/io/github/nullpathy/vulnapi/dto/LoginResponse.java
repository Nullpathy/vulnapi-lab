package io.github.nullpathy.vulnapi.dto;

import io.github.nullpathy.vulnapi.entity.Role;

public record LoginResponse(
        Long id,
        String email,
        Role role,
        String token
) {}