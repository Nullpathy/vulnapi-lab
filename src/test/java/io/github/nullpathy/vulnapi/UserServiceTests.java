package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUserWithUserRole() {
        User user = userService.createUser(
                "Service Test User",
                "service-create-test@vulnapi.local",
                "temporary-password"
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getRole().name()).isEqualTo("USER");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userService.createUser(
                "Alice",
                "duplicate@vulnapi.local",
                "temporary-password"
        );

        assertThatThrownBy(() ->
                userService.createUser(
                        "Another Alice",
                        "duplicate@vulnapi.local",
                        "another-password"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");
    }
}