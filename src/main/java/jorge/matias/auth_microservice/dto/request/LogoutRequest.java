package jorge.matias.auth_microservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(

    @NotBlank(message = "{validation.error.empty_token}")
    String refreshToken
) {}