package jorge.matias.auth_microservice.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AuthException {

    public UserNotFoundException() {
        super("auth.error.user_not_found", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String username) {
        super("auth.error.user_not_found", HttpStatus.NOT_FOUND, username);
    }

    public UserNotFoundException(Object... args) {
        super("auth.error.user_not_found", HttpStatus.NOT_FOUND, args);
    }
}
