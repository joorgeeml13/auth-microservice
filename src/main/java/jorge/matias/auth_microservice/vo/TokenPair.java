package jorge.matias.auth_microservice.vo;

import lombok.*;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken
) {}

