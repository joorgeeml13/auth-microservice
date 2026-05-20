package jorge.matias.auth_microservice.config;

import java.net.Authenticator;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jorge.matias.auth_microservice.model.auth.AccountPrincipal;
import jorge.matias.auth_microservice.model.entity.Account;
import jorge.matias.auth_microservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AccountRepository accountRepository;

    @Bean
    public UserDetailsService userDetailsService(){
        return id -> {
            Account acc;
            try{
                acc = accountRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new UsernameNotFoundException("auth.error.user_not_found"));
            }catch(IllegalArgumentException e){
                acc = accountRepository.findByEmail(id)
                    .orElseThrow(() -> new UsernameNotFoundException("auth.error.user_not_found"));
            }
            
           var authorities = acc.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();

            return AccountPrincipal.builder()
                    .principal(acc)
                    .authorities(authorities)
                    .build();
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
     @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
