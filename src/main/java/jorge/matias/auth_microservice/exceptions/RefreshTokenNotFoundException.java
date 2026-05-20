package jorge.matias.auth_microservice.exceptions;



import org.springframework.http.HttpStatus;


public class RefreshTokenNotFoundException extends AuthException {

    public RefreshTokenNotFoundException() {
        super("auth.error.refresh-token-not-found", HttpStatus.NOT_FOUND);
    }

    public RefreshTokenNotFoundException(String tokenId) {
        super("auth.error.refresh-token-not-found", HttpStatus.NOT_FOUND, tokenId);
    }
}

