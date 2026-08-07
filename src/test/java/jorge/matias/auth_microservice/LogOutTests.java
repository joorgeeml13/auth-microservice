package jorge.matias.auth_microservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import jorge.matias.auth_microservice.dto.request.LogoutRequest;
import jorge.matias.auth_microservice.model.entity.Account;
import jorge.matias.auth_microservice.model.entity.RefreshToken;
import jorge.matias.auth_microservice.repository.AccountRepository;
import jorge.matias.auth_microservice.repository.RefreshTokenRepository;

@AutoConfigureMockMvc
public class LogOutTests extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String DEVICE_ID = "mi-iphone-guapo-123";

    @AfterEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should revoke all tokens and return 401 if device id does not match")
    void shouldRevokeAllOnDeviceIdMismatch() throws Exception {
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

        LogoutRequest request = new LogoutRequest(refreshToken);

        mockMvc.perform(post(contextPath + "/auth/logout")
                .contextPath(contextPath)
                .header("X-Device-ID", "device-hacker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should logout successfully for Mobile client using request body")
    void happyPathLogoutMobile() throws Exception {
        String registerJson = """
            {"name":"Logout Mobile","email":"logout-mobile@test.com","password":"Password123!"}
            """;
        mockMvc.perform(post(contextPath + "/auth/register")
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
            {"email":"logout-mobile@test.com","password":"Password123!"}
            """;
        MvcResult loginResult = mockMvc.perform(post(contextPath + "/auth/login")
                .contextPath(contextPath)
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("refreshToken").asText();

        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);

        mockMvc.perform(post(contextPath + "/auth/logout")
                .contextPath(contextPath)
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.findByToken(refreshToken)).isPresent();
        assertThat(refreshTokenRepository.findByToken(refreshToken).get().isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should logout successfully for Web client using cookie and clear cookie")
    void happyPathLogoutWeb() throws Exception {
        String registerJson = """
            {"name":"Logout Web","email":"logout-web@test.com","password":"Password123!"}
            """;
        mockMvc.perform(post(contextPath + "/auth/register")
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
            {"email":"logout-web@test.com","password":"Password123!"}
            """;
        MvcResult loginResult = mockMvc.perform(post(contextPath + "/auth/login")
                .contextPath(contextPath)
                .header("X-Client-Type", "WEB")
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");
        assertThat(refreshCookie).isNotNull();

        mockMvc.perform(post(contextPath + "/auth/logout")
                .contextPath(contextPath)
                .header("X-Client-Type", "WEB")
                .header("X-Device-ID", DEVICE_ID)
                .cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));

        assertThat(refreshTokenRepository.findByToken(refreshCookie.getValue())).isPresent();
        assertThat(refreshTokenRepository.findByToken(refreshCookie.getValue()).get().isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should return 404 Not Found when logging out with a non-existent token")
    void logoutNonExistentToken() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest(UUID.randomUUID().toString());

        mockMvc.perform(post(contextPath + "/auth/logout")
                .contextPath(contextPath)
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when logging out without token (no cookie and no body)")
    void logoutMissingToken() throws Exception {
        mockMvc.perform(post(contextPath + "/auth/logout")
                .contextPath(contextPath)
                .header("X-Device-ID", DEVICE_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when X-Device-ID header is missing")
    void logoutMissingDeviceIdHeader() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest("some-token");

        mockMvc.perform(post(contextPath + "/auth/logout")
                .contextPath(contextPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when logging out with an expired token")
    void logoutExpiredToken() throws Exception {
        Account account = Account.builder()
                .email("expired@test.com")
                .password("encoded-pass")
                .name("Expired User")
                .build();
        account = accountRepository.save(account);

        String expiredTokenVal = UUID.randomUUID().toString();
        RefreshToken expiredToken = RefreshToken.builder()
                .token(expiredTokenVal)
                .account(account)
                .deviceId(DEVICE_ID)
                .expiryDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(expiredToken);

        LogoutRequest logoutRequest = new LogoutRequest(expiredTokenVal);

        mockMvc.perform(post(contextPath + "/auth/logout")
                .contextPath(contextPath)
                .header("X-Device-ID", DEVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }
}
