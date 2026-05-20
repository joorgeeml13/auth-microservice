package jorge.matias.auth_microservice.services;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jorge.matias.auth_microservice.exceptions.AccountAlreadyExistException;
import jorge.matias.auth_microservice.exceptions.UserNotFoundException;
import jorge.matias.auth_microservice.model.auth.AccountPrincipal;
import jorge.matias.auth_microservice.model.entity.Account;
import jorge.matias.auth_microservice.model.entity.RefreshToken;
import jorge.matias.auth_microservice.repository.AccountRepository;
import jorge.matias.auth_microservice.vo.TokenPair;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AccountRepository accountRepository;
    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void registerAccount(String name, String email, String password){
        
        if(accountRepository.findByEmail(email).isPresent())
            throw new AccountAlreadyExistException();

        String finalName;
        if(name != null && !name.trim().isEmpty())
            finalName = name.trim();
        else
            finalName = "User@" + java.util.UUID.randomUUID().toString().substring(0, 8);

        Account account = Account.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .name(finalName)
            .build();

        accountRepository.save(account);
    }

    @Transactional
    public TokenPair login(String email, String password, String deviceId){
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        AccountPrincipal principal = (AccountPrincipal) auth.getPrincipal();
        Account account = accountRepository.findById(principal.getPrincipal().getId())
            .orElseThrow(UserNotFoundException::new);

        String accessToken = jwtService.generateToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(account, deviceId);

        return TokenPair.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    @Transactional
    public TokenPair refreshToken(String oldRefreshToken, String deviceId) {
        RefreshToken newRefreshTokenEntity = refreshTokenService.rotateRefreshToken(oldRefreshToken, deviceId);
        Account account = newRefreshTokenEntity.getAccount();
        AccountPrincipal principal = buildPrincipal(account);
        String newAccessToken = jwtService.generateToken(principal);

        return TokenPair.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshTokenEntity.getToken())
            .build();
    }

    private AccountPrincipal buildPrincipal(Account acc){
        return AccountPrincipal.builder()
            .principal(acc)
            .authorities(acc.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList()))
            .build();
    }
}
