package jorge.matias.auth_microservice;

import jorge.matias.auth_microservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
public class RegisterTests extends AbstractIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanDatabase() {
        accountRepository.deleteAll();
    }
    

    @Test
    void goodPathWithName() throws Exception{
        String json = """
            {"name":"test1","email": "test1@test.com","password":"Password123@"}
            """;
        
        mockMvc.perform(post(contextPath + "/auth/register")
                .contextPath(contextPath)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        var account = accountRepository.findByEmail("test1@test.com").orElseThrow();
        assertThat(account.getName()).isEqualTo("test1"); // Comprueba el explícito
        assertThat(account.getPassword()).isNotEqualTo("Password123@");
        assertThat(account.getPassword()).startsWith("$2a$");
    }

    @Test
    void goodPathWithoutName() throws Exception{
        String json = """
            {"email": "anonimo@test.com","password":"Password123@"}
            """;
            
        mockMvc.perform(post(contextPath + "/auth/register")
                .contextPath(contextPath)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        var account = accountRepository.findByEmail("anonimo@test.com").orElseThrow();
        assertThat(account.getName()).isNotBlank();
        assertThat(account.getName()).startsWith("User@");
        assertThat(account.getName()).hasSize(13);
        
        assertThat(account.getPassword()).isNotEqualTo("Password123@");
        assertThat(account.getPassword()).startsWith("$2a$");
    }

    @Test
    void badRequestCloneUser() throws Exception{
        String json = """
            {
                "name": "original",
                "email": "clon@test.com",
                "password": "Password123!"
            }
            """;
        mockMvc.perform(post(contextPath + "/auth/register")
                .contextPath(contextPath)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contextPath(contextPath)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_ERROR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        """
        {"name": "a", "email": "sin-arroba.com", "password": "pass"}
        """,
        """
        {"name": "a", "email": "valid@mail.com", "password": ""}
        """,
        """
        {"name": "a", "email": "", "password": "pass"}
        """
    })
    void validationFails_returnsBadRequest(String invalidJson) throws Exception {
        mockMvc.perform(post(contextPath + "/auth/register")
                .contextPath(contextPath)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
        
        assertThat(accountRepository.count()).isEqualTo(0);
    }
}