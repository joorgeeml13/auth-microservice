package jorge.matias.auth_microservice;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class PublicKeyTests extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPublicKeyPem_ReturnsPublicKeyWithSecurityHeaders() throws Exception {
        mockMvc.perform(get(contextPath + "/auth/public-key.pem")
                .contextPath(contextPath))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/x-pem-file"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, immutable"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().string(containsString("-----BEGIN PUBLIC KEY-----")))
                .andExpect(content().string(containsString("-----END PUBLIC KEY-----")));
    }
}
