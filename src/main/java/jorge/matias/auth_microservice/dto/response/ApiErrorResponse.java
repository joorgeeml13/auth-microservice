package jorge.matias.auth_microservice.dto.response;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    LocalDateTime timestamp,
    Integer status,
    String code,
    String message,
    String path
) {}

