package io.github.nullpathy.vulnapi;

import io.github.nullpathy.vulnapi.entity.Role;
import io.github.nullpathy.vulnapi.entity.User;
import io.github.nullpathy.vulnapi.repository.UserRepository;
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
class AdminUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRejectUnauthenticatedAccessToAdminUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRegularUserAccessToAdminUsers() throws Exception {
        User user = userService.createUser(
                "Regular User",
                "regular-admin-user-test@vulnapi.local",
                "SecurePassword123"
        );

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldExposePasswordHashesInAdminUserResponse() throws Exception {

        User alice = userService.createUser(
                "Alice",
                "alice-admin-user-test@vulnapi.local",
                "SecurePassword123"
        );

        User bob = userService.createUser(
                "Bob",
                "bob-admin-user-test@vulnapi.local",
                "SecurePassword123"
        );

        User admin = userService.createUser(
                "Admin",
                "admin-user-test@vulnapi.local",
                "SecurePassword123"
        );

        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        String adminToken = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'alice-admin-user-test@vulnapi.local')].password").exists())
                .andExpect(jsonPath("$[?(@.email == 'bob-admin-user-test@vulnapi.local')].password").exists())
                .andExpect(jsonPath("$[?(@.email == 'admin-user-test@vulnapi.local')].password").exists());
    }
}