package jorge.matias.auth_microservice.controller;

import jorge.matias.auth_microservice.repository.AccountRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jorge.matias.auth_microservice.config.Constantes;
import jorge.matias.auth_microservice.dto.request.LoginRequest;
import jorge.matias.auth_microservice.dto.request.RefreshRequest;
import jorge.matias.auth_microservice.dto.request.RegisterRequest;
import jorge.matias.auth_microservice.dto.response.AuthResponse;
import jorge.matias.auth_microservice.exceptions.RefreshTokenNotFoundException;
import jorge.matias.auth_microservice.services.AuthService;
import jorge.matias.auth_microservice.vo.TokenPair;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
public class AuthController {
    
    private final AuthService authService;

    @Value("${security.jwt.refresh-cookie-name}")
    private String refreshCookieName;

    @Value("${security.jwt.refresh-expiration-days}")
    private long refreshCookieMaxAgeDays;

    @Value("${security.jwt.refresh-cookie.secure}")
    private boolean refreshCookieSecure;

    @Value("${security.jwt.refresh-path}")
    private String refreshPath;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.registerAccount(
            request.name(),
            request.email(),
            request.password()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @RequestBody @Valid LoginRequest request,
        @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
        @RequestHeader(value = "X-Device-ID", defaultValue = "web-browser") String deviceId,
        HttpServletResponse response
    ) {

        TokenPair tokens = authService.login(request.email(), request.password(), deviceId);

        if(Constantes.CLIENT_MOBILE.equalsIgnoreCase(clientType))
            return ResponseEntity.ok(new AuthResponse(tokens.accessToken(), tokens.refreshToken()));

        ResponseCookie cookie = createRefreshCookie(tokens.refreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(new AuthResponse(tokens.accessToken(), null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
        @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType,
        @RequestHeader(value = "X-Device-ID", defaultValue = "web-browser") String deviceId,
        @CookieValue(name = "${security.jwt.refresh-cookie-name}", required = false) String refreshTokenCookie,
        @RequestBody(required = false) RefreshRequest refreshBody
    ) {
        String tokenToRefresh = null;

        if (Constantes.CLIENT_MOBILE.equalsIgnoreCase(clientType)) {
            if (refreshBody == null || refreshBody.refreshToken() == null || refreshBody.refreshToken().isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.refresh.token.missing.body");
            }
            tokenToRefresh = refreshBody.refreshToken();
        } else {
            if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
                throw new RefreshTokenNotFoundException();
            }
            tokenToRefresh = refreshTokenCookie;
        }

        TokenPair newTokens = authService.refreshToken(tokenToRefresh, deviceId);

        if (Constantes.CLIENT_MOBILE.equalsIgnoreCase(clientType)) {
            return ResponseEntity.ok(new AuthResponse(newTokens.accessToken(), newTokens.refreshToken()));
        }

        ResponseCookie cookie = createRefreshCookie(newTokens.refreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(new AuthResponse(newTokens.accessToken(), null));
    }
    

    private ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshPath)                           
                .maxAge(refreshCookieMaxAgeDays * 24 * 60 * 60) 
                .sameSite("Strict")                         
                .build();
    }
}
