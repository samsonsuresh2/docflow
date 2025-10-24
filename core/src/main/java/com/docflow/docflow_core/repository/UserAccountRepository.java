package com.docflow.docflow_core.repository;

import com.docflow.docflow_core.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsernameAndActiveTrue(String username);
}
