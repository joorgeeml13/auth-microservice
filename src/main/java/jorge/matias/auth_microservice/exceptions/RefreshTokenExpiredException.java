package jorge.matias.auth_microservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando un refresh token ha expirado.
 * El cliente debe solicitar un nuevo login para obtener un nuevo token.
 * 
 * Código HTTP: 401 UNAUTHORIZED (token expirado)
 * Mensaje i18n: auth.error.refresh-token-expired
 */
public class RefreshTokenExpiredException extends AuthException {

    public RefreshTokenExpiredException() {
        super("auth.error.refresh-token-expired", HttpStatus.UNAUTHORIZED);
    }

    public RefreshTokenExpiredException(String expiryDate) {
        super("auth.error.refresh-token-expired", HttpStatus.UNAUTHORIZED, expiryDate);
    }
}

