package jorge.matias.auth_microservice.config;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jorge.matias.auth_microservice.dto.response.ApiErrorResponse;
import jorge.matias.auth_microservice.exceptions.AuthException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final MessageSource messageSource;

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(
        AuthException ex,
        HttpServletRequest request
    ){

        String translatedMessage = messageSource.getMessage(
                ex.getMessageKey(), 
                ex.getArgs(), 
                "Error desconocido",
                LocaleContextHolder.getLocale() 
        );

        return buildResponse(ex.getHttpStatus(), Constantes.AUTH_ERROR_CODE, translatedMessage, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ){
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> {
                String message = error.getDefaultMessage();

                return messageSource.getMessage(
                    message != null ? message : "error.bad_request",
                    null,
                    message,
                    LocaleContextHolder.getLocale()
                );
            })
            .collect(Collectors.joining(" | "));

            return buildResponse(HttpStatus.BAD_REQUEST, Constantes.VALIDATION_ERROR_CODE, errors, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleSpringSecurityAuthException(
        AuthenticationException ex,
        HttpServletRequest request
    ){
        String messageKey = "auth.error.invalid_credentials";

        if(ex instanceof DisabledException)
            messageKey = "auth.error.account_disabled";
        else if (ex instanceof LockedException)
            messageKey = "auth.error.account_locked";

        String translatedMessage = messageSource.getMessage(
            messageKey,
            null,
            "Bad Credentials",
            LocaleContextHolder.getLocale()
        );

        return buildResponse(HttpStatus.UNAUTHORIZED, Constantes.AUTH_ERROR_CODE, translatedMessage, request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
        HttpStatus status, 
        String code, 
        String message, 
        HttpServletRequest request
    ) {
        ApiErrorResponse res = new ApiErrorResponse(
            LocalDateTime.now(),
            status.value(),
            code,
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(res);
    }
}
