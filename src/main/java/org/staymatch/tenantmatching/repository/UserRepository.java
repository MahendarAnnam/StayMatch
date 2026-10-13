package org.staymatch.tenantmatching.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.staymatch.tenantmatching.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
}

