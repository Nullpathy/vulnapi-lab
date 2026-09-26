package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.exception.InvalidCredentialsException;
import io.github.nullpathy.vulnapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateUserWithUserRole() {
        User user = userService.createUser(
                "Service Test User",
                "service-create-test@vulnapi.local",
                "temporary-password"
        );

        assertThat(user.getId()).isNotNull();
        assertThat(user.getRole().name()).isEqualTo("USER");

        assertThat(user.getPassword())
                .isNotEqualTo("temporary-password");

        assertThat(user.getPassword())
                .startsWith("$2");

        assertThat(
                passwordEncoder.matches(
                        "temporary-password",
                        user.getPassword()
                )
        ).isTrue();
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

    @Test
    void shouldAuthenticateUserWithValidCredentials() {
        User createdUser = userService.createUser(
                "Auth Test User",
                "auth-valid@vulnapi.local",
                "SecurePassword123"
        );

        User authenticatedUser = userService.authenticate(
                "auth-valid@vulnapi.local",
                "SecurePassword123"
        );

        assertThat(authenticatedUser.getId())
                .isEqualTo(createdUser.getId());

        assertThat(authenticatedUser.getEmail())
                .isEqualTo("auth-valid@vulnapi.local");
    }

    @Test
    void shouldRejectAuthenticationWithInvalidPassword() {
        userService.createUser(
                "Auth Test User",
                "auth-invalid-password@vulnapi.local",
                "SecurePassword123"
        );

        assertThatThrownBy(() ->
                userService.authenticate(
                        "auth-invalid-password@vulnapi.local",
                        "WrongPassword123"
                )
        )
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void shouldRejectAuthenticationWithUnknownEmail() {
        assertThatThrownBy(() ->
                userService.authenticate(
                        "unknown@vulnapi.local",
                        "SecurePassword123"
                )
        )
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}