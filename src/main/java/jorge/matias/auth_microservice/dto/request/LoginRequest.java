package jorge.matias.auth_microservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Email(message = "auth.validation.invalid-email")
    @NotBlank(message = "auth.validation.email-required")
    String email,

    @NotBlank(message = "auth.validation.password-required")
    String password
) 
{}
