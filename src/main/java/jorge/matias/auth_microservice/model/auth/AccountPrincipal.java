package jorge.matias.auth_microservice.model.auth;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jorge.matias.auth_microservice.model.entity.Account;
import jorge.matias.auth_microservice.model.entity.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AccountPrincipal implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private final Account principal;
     private final Collection<? extends GrantedAuthority> authorities;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return principal.getPassword();
    }

    @Override
    public String getUsername() {
        return principal.getId().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return principal.getStatus() != AccountStatus.BANNED 
            && principal.getStatus() != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return principal.getStatus() == AccountStatus.ACTIVE;
    }
}