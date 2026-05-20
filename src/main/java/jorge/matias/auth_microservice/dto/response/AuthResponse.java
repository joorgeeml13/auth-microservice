package jorge.matias.auth_microservice.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {}

