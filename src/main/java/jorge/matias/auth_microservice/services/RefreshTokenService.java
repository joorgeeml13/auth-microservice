package jorge.matias.auth_microservice.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jorge.matias.auth_microservice.exceptions.RefreshTokenCompromisedException;
import jorge.matias.auth_microservice.exceptions.RefreshTokenExpiredException;
import jorge.matias.auth_microservice.exceptions.RefreshTokenNotFoundException;
import jorge.matias.auth_microservice.model.entity.Account;
import jorge.matias.auth_microservice.repository.RefreshTokenRepository;
import jorge.matias.auth_microservice.model.entity.RefreshToken;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String createRefreshToken(Account acc, String deviceId){
        refreshTokenRepository.revokeByAccountAndDeviceId(acc.getId(), deviceId);

        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
            .token(tokenValue)
            .account(acc)
            .deviceId(deviceId)
            .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();
        
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenValue, String deviceId){
        RefreshToken oldToken = refreshTokenRepository.findByToken(oldTokenValue)
            .orElseThrow(RefreshTokenNotFoundException::new);

         if (oldToken.isRevoked()) {
            refreshTokenRepository.revokeByAccountAndDeviceId(oldToken.getAccount().getId(), deviceId);
            throw new RefreshTokenCompromisedException(deviceId);
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new RefreshTokenExpiredException(oldToken.getExpiryDate().toString());
        }

        String newTokenValue = UUID.randomUUID().toString();
        RefreshToken newToken = RefreshToken.builder()
            .token(newTokenValue)
            .account(oldToken.getAccount())
            .deviceId(deviceId)
            .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();

        RefreshToken savedNewToken = refreshTokenRepository.save(newToken);

        oldToken.setRevoked(true);
        oldToken.setReplacedBy(savedNewToken);
        refreshTokenRepository.save(oldToken);

        return savedNewToken;
    }

    @Transactional
    public void revokeAllTokens(Account account){
        List<RefreshToken> validTokens = refreshTokenRepository.findAllByAccountAndRevokedFalse(account);

        if (validTokens.isEmpty()) return;

        validTokens.forEach(token -> token.setRevoked(true));

        refreshTokenRepository.saveAll(validTokens);
    }
}
