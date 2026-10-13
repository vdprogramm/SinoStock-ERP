package com.sinostock.erp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SinoStockApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private String adminToken;
    private String staffToken;

    @BeforeEach
    void login() throws Exception {
        adminToken = loginAs("admin", "Admin@12345");
        staffToken = loginAs("staff", "Staff@12345");
    }

    String loginAs(String username, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new Login(username, password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    record Login(String username, String password) {}

    long createProduct(int stock) throws Exception {
        String sku = "TEST-" + UUID.randomUUID().toString().substring(0, 8);
        String body = """
            {"sku":"%s","nameCn":"测试商品","nameEn":"Test Product", "priceCny":25.50,
             "stockQuantity":%d,"lowStockThreshold":2}
            """.formatted(sku, stock);
        String response = mvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    long createOrder(long productId, int quantity) throws Exception {
        String body = """
            {"items":[{"productId":%d,"quantity":%d}]}
            """.formatted(productId, quantity);
        String response = mvc.perform(post("/api/orders").header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    JsonNode product(long productId) throws Exception {
        String response = mvc.perform(get("/api/products/{id}", productId)
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    @Test
    void sixRolesHaveSeparatedPermissions() throws Exception {
        String createUser = """
          {"username":"acc_%s","password":"UniquePass123!","role":"ACCOUNTANT"}
          """.formatted(UUID.randomUUID().toString().substring(0,8));
        String response = mvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(createUser))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("ACCOUNTANT"))
                .andReturn().getResponse().getContentAsString();
        String username = json.readTree(response).get("username").asText();
        String accountant = loginAs(username, "UniquePass123!");
        mvc.perform(get("/api/accounting/receivables")
                .header("Authorization", "Bearer " + accountant)).andExpect(status().isOk());
        mvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + accountant)).andExpect(status().isOk());
        mvc.perform(get("/api/suppliers")
                .header("Authorization", "Bearer " + accountant)).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + accountant)
                .contentType(MediaType.APPLICATION_JSON).content(createUser)).andExpect(status().isForbidden());
    }

    @Test
    void fullOrderLifecycleDeductsAndRestoresStock() throws Exception {
        long productId = createProduct(5);
        long orderId = createOrder(productId, 3);
        assertEquals(5, product(productId).get("stockQuantity").asInt());
        mvc.perform(patch("/api/orders/{id}/confirm", orderId)
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        assertEquals(2, product(productId).get("stockQuantity").asInt());
        mvc.perform(patch("/api/orders/{id}/cancel", orderId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        assertEquals(5, product(productId).get("stockQuantity").asInt());
        mvc.perform(patch("/api/orders/{id}/cancel", orderId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    void insufficientStockRollsBack() throws Exception {
        long productId = createProduct(2);
        long orderId = createOrder(productId, 3);
        mvc.perform(patch("/api/orders/{id}/confirm", orderId)
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isConflict());
        assertEquals(2, product(productId).get("stockQuantity").asInt());
        mvc.perform(get("/api/orders/{id}", orderId)
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void pendingCancelDoesNotChangeStock() throws Exception {
        long productId = createProduct(6);
        long orderId = createOrder(productId, 2);
        mvc.perform(patch("/api/orders/{id}/cancel", orderId)
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        assertEquals(6, product(productId).get("stockQuantity").asInt());
    }

    @Test
    void jwtAndRoleAuthorization() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dailyRevenue.length()").value(7));
    }

    @Test
    void adminAdjustsStockAndWritesHistory() throws Exception {
        long id = createProduct(2);
        mvc.perform(patch("/api/products/{id}/stock", id)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"changeQuantity\":4,\"reason\":\"RESTOCK\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stockQuantity").value(6));
        mvc.perform(get("/api/inventory/logs").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/inventory/logs").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
