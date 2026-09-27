package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.security.JwtService;
import io.github.nullpathy.vulnapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRejectUnauthenticatedAccessToCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void shouldReturnAuthenticatedUsersOwnProfile() throws Exception {
        User user = userService.createUser(
                "Profile Test User",
                "profile-test@vulnapi.local",
                "SecurePassword123"
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}