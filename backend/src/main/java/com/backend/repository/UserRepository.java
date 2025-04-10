package com.backend.repository;

import com.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u")
    Collection<User> allUsers();

    Optional<User> findByEmail(String email);

}
