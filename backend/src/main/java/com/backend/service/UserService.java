package com.backend.service;

import com.backend.dto.RegisterRequest;
import com.backend.model.User;
import com.backend.repository.UserRepository;
import com.backend.config.JwtUtil; // Asigură-te că importul este corect
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;  // Injectam JwtUtil

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<User> checkCredentials(String email, String rawPassword) {
        try {
            logger.info("Attempting to check credentials for email: {}", email);
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPassword());
                logger.info("Password match result for {}: {}", email, passwordMatches);
                return passwordMatches ? userOpt : Optional.empty();
            }
            logger.info("No user found for email: {}", email);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error checking credentials for email: {}", email, e);
            throw e;
        }
    }

    public Collection<User> getAllUsers() {
        return userRepository.allUsers();
    }

    public boolean checkUserById(Integer id) {
        return userRepository.existsById(id);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String generateJwtForUser(String email)
    {
        try {
            logger.info("Generating JWT token for user: {}", email);
            return jwtUtil.generateToken(email);
        } catch (Exception e) {
            logger.error("Error generating JWT token for user: {}", email, e);
            throw e;
        }
    }

    public boolean checkIfExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void createUser(RegisterRequest req) {
        try {
            logger.info("Creating new user with email: {}", req.getEmail());
            User u = new User();
            u.setFirstName(req.getFirstName());
            u.setLastName(req.getLastName());
            u.setEmail(req.getEmail());
            u.setRole(req.getRole());
            u.setPassword(passwordEncoder.encode(req.getPassword()));
            userRepository.save(u);
            logger.info("User created successfully: {}", req.getEmail());
        } catch (Exception e) {
            logger.error("Error creating user: {}", req.getEmail(), e);
            throw e;
        }
    }
}
