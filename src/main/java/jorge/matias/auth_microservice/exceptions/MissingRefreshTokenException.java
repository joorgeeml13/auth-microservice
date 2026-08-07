package jorge.matias.auth_microservice.exceptions;

import org.springframework.http.HttpStatus;

public class MissingRefreshTokenException extends AuthException {
    private static final long serialVersionUID = 1L;

    public MissingRefreshTokenException() {
        super("validation.error.empty_token", HttpStatus.BAD_REQUEST);
    }
}
