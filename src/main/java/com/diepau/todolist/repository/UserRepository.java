package com.diepau.todolist.repository;

import com.diepau.todolist.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Data access for users. Spring Data JPA generates the queries from the method names.
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
