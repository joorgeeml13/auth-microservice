package jorge.matias.auth_microservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jorge.matias.auth_microservice.repository.AccountRepository;
import jorge.matias.auth_microservice.repository.RefreshTokenRepository;

@AutoConfigureMockMvc
public class RefreshTests extends AbstractIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String validRefreshTokenMobile;
    private final String DEVICE_ID = "mi-iphone-guapo-123";

    @BeforeEach
    void setUp() throws Exception {
        // 1. Registramos al pavo
        String registerJson = """
            {"name" : "Test1", "email" : "refresh@test.com", "password" : "Password123!"}
            """;
        mockMvc.perform(post("/api/v1/auth/register").contextPath(contextPath).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(registerJson)).andExpect(status().isCreated());

        String loginJson = """
            {"email":"refresh@test.com","password":"Password123!"}
            """;
        MvcResult result = mockMvc.perform(post(contextPath + "/auth/login")
                .contextPath(contextPath).with(csrf())
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().isOk()).andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        this.validRefreshTokenMobile = responseJson.get("refreshToken").asText();
    }

    @AfterEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void happyPathRefreshMobile() throws Exception {
        String refreshJson = """
            {"refreshToken": "%s"}
            """.formatted(validRefreshTokenMobile);

        mockMvc.perform(post(contextPath + "/auth/refresh")
                .contextPath(contextPath).with(csrf())
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson))
                .andExpect(status().isOk());
                
    }

    @Test
    void compromisedTokenExplodes() throws Exception {
        String refreshJson = """
            {"refreshToken": "%s"}
            """.formatted(validRefreshTokenMobile);

        mockMvc.perform(post(contextPath + "/auth/refresh")
                .contextPath(contextPath).with(csrf())
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson))
                .andExpect(status().isOk());

        mockMvc.perform(post(contextPath + "/auth/refresh")
                .contextPath(contextPath).with(csrf())
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson))
                .andExpect(status().isUnauthorized());
    }
}
