package jorge.matias.auth_microservice.config;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jorge.matias.auth_microservice.dto.response.ApiErrorResponse;
import jorge.matias.auth_microservice.exceptions.AuthException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final MessageSource messageSource;

    @ExceptionHandler(AuthException.class)
    public ResponseEntity handleAuthExcepption(
        AuthException ex,
        HttpServletRequest request
    ){

        String translatedMessage = messageSource.getMessage(
                ex.getMessageKey(), 
                null, 
                "Error desconocido",
                LocaleContextHolder.getLocale() 
        );

        ApiErrorResponse res = new ApiErrorResponse(
            LocalDateTime.now(),
            ex.getHttpStatus().value(),
            Constantes.AUTH_ERROR_CODE,
            translatedMessage,
            request.getRequestURI()
            
        );

        return ResponseEntity.badRequest().body(res);
    }
}
