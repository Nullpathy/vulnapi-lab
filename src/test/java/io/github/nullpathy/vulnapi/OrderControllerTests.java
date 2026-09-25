package io.github.nullpathy.vulnapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectUnauthenticatedAccessToOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedAccessToOrderById() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedOrderCreation() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": 9,
                                  "productId": 1,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}