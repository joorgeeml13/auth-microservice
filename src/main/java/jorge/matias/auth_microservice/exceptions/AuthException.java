package jorge.matias.auth_microservice.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class AuthException extends RuntimeException{

    private final String messageKey;
    private final Object[] args;
    private final HttpStatus httpStatus;

    protected AuthException(String messageKey, HttpStatus httpStatus, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
        this.args = args;
    }
}
