package jorge.matias.auth_microservice.config;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

        return buildResponse(ex.getHttpStatus(), Constantes.AUTH_ERROR_CODE, translatedMessage, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationException(
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
