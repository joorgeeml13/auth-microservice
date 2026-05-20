package jorge.matias.auth_microservice.exceptions;

import org.springframework.http.HttpStatus;

public class RefreshTokenCompromisedException extends AuthException {

    public RefreshTokenCompromisedException() {
        super("auth.error.refresh-token-compromised", HttpStatus.UNAUTHORIZED);
    }

    public RefreshTokenCompromisedException(String deviceId) {
        super("auth.error.refresh-token-compromised", HttpStatus.UNAUTHORIZED, deviceId);
    }
}

