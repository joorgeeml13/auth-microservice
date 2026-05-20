package jorge.matias.auth_microservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Size(max = 127, message = "validation.error.field_too_long")
    String name,

    @NotBlank(message = "validation.error.field_required")
    @Email(message = "validation.error.invalid_forma")
    String email,
        
    @NotBlank(message = "validation.error.field_required")
    String password
) {}
