package com.accountservice.repository;

import com.accountservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_WithExistingUser_ShouldReturnUser() {
        // Arrange
        User user = new User("testUser", "password");
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> result = userRepository.findByUsername("testUser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
    }

    @Test
    void findByUsername_WithNonExistentUser_ShouldReturnEmpty() {
        // Arrange
        User user = new User("existingUser", "password");
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> result = userRepository.findByUsername("nonExistentUser");

        // Assert
        assertFalse(result.isPresent());
    }
}
