package jorge.matias.auth_microservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import com.fasterxml.jackson.databind.ObjectMapper;

import jorge.matias.auth_microservice.dto.request.RefreshRequest;
import jorge.matias.auth_microservice.repository.AccountRepository;

@AutoConfigureMockMvc
public class LogOutTests extends AbstractIntegrationTest{
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String DEVICE_ID = "mi-iphone-guapo-123";
    
    @BeforeEach
    void setUp() throws Exception{
        
    }

    @AfterEach
    void cleanDatabase() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should revoke all tokens and throw an exception if the device id does not match")
    void shouldRevokeAllOnDeviceIdMismatch() throws Exception{
        String registerJson = """
            {"name":"Logout Test","email":"logout@test.com","password":"Password123!"}
            """;

        mockMvc.perform(post(contextPath + "/auth/register")
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
            {"email":"logout@test.com","password":"Password123!"}
            """;

        var loginResult = mockMvc.perform(post(contextPath + "/auth/login")
                .contextPath(contextPath)
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("refreshToken").asText();

        RefreshRequest request = new RefreshRequest(refreshToken);

        mockMvc.perform(post(contextPath + "/auth/logout")
                    .contextPath(contextPath)
                    .header("X-Device-ID", "device-hacker")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(result -> System.out.println(result.getResponse().getStatus() + " | " + result.getResponse().getContentAsString()))
                    .andExpect(status().isUnauthorized());
    }
}
