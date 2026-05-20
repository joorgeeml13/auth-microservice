package jorge.matias.auth_microservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import jorge.matias.auth_microservice.model.entity.Account;

public interface AccountRepository extends JpaRepository<Account, UUID>{
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
}
