package jorge.matias.auth_microservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jorge.matias.auth_microservice.repository.AccountRepository;



@AutoConfigureMockMvc
public class LogInTests extends AbstractIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;
    
    @BeforeEach
    void setUp() throws Exception{
        String registerJson = """
            {
                "name" : "Test1",
                "email" : "test@test.com",
                "password" : "Password123!"
            }
            """;
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contextPath(contextPath)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void cleanDatabase() {
        accountRepository.deleteAll();
    }

    @Test
    void validLoginWeb() throws Exception{
        String json = """
            {"email":"test@test.com","password":"Password123!"}
            """;

        mockMvc.perform(post(contextPath + "/auth/login")
            .contextPath(contextPath)
            .with(csrf())
            .header("X-Client-Type", "WEB")
            .header("X-Device-ID", "test-device-uuid-1234")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isEmpty())
            .andExpect(cookie().exists("refresh_token"))
            .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void validLoginMobile() throws Exception{
        String json = """
            {"email":"test@test.com","password":"Password123!"}
            """;
        
        mockMvc.perform(post(contextPath + "/auth/login")
                .contextPath(contextPath)
                .with(csrf())
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", "test-device-uuid-5678")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(cookie().doesNotExist("refresh_token"));
    }

    @Test
    void invalidLoginMissingDeviceId() throws Exception {
        String json = """
            {"email":"test@test.com","password":"Password123!"}
            """;
        
        mockMvc.perform(post(contextPath + "/auth/login")
                .contextPath(contextPath)
                .with(csrf())
                .header("X-Client-Type", "WEB")
                // NO mandamos el X-Device-ID aposta
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}