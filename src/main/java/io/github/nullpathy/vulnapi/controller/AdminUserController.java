package io.github.nullpathy.vulnapi.controller;

import io.github.nullpathy.vulnapi.dto.UserResponse;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = userService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}