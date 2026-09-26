package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Role;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTests {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateJwtToken() {
        User user = new User(
                "JWT Test User",
                "jwt-test@vulnapi.local",
                "irrelevant-password",
                Role.USER
        );

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();

        assertThat(token.split("\\."))
                .hasSize(3);
    }

    @Test
    void shouldExtractClaimsFromValidToken() {
        User user = new User(
                "JWT Test User",
                "jwt-claims@vulnapi.local",
                "irrelevant-password",
                Role.USER
        );

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token))
                .isTrue();

        assertThat(jwtService.extractEmail(token))
                .isEqualTo("jwt-claims@vulnapi.local");

        assertThat(jwtService.extractRole(token))
                .isEqualTo("USER");
    }

    @Test
    void shouldRejectTamperedToken() {
        User user = new User(
                "JWT Test User",
                "jwt-tampered@vulnapi.local",
                "irrelevant-password",
                Role.USER
        );

        String token = jwtService.generateToken(user);

        String[] parts = token.split("\\.");

        String tamperedPayload = parts[1].substring(0, parts[1].length() - 1)
                + (parts[1].endsWith("A") ? "B" : "A");

        String tamperedToken =
                parts[0] + "." + tamperedPayload + "." + parts[2];

        assertThat(jwtService.isTokenValid(tamperedToken))
                .isFalse();
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        String testSecret =
                "this-is-a-test-secret-key-with-at-least-32-bytes";

        JwtService shortLivedJwtService =
                new JwtService(testSecret, 1);

        User user = new User(
                "JWT Expiration Test",
                "jwt-expired@vulnapi.local",
                "irrelevant-password",
                Role.USER
        );

        String token = shortLivedJwtService.generateToken(user);

        Thread.sleep(1000);

        assertThat(shortLivedJwtService.isTokenValid(token))
                .isFalse();
    }
}