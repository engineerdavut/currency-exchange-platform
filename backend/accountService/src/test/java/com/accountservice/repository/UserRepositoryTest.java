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

        User user = new User("testUser", "password");
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByUsername("testUser");

        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
    }

    @Test
    void findByUsername_WithNonExistentUser_ShouldReturnEmpty() {

        User user = new User("existingUser", "password");
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> result = userRepository.findByUsername("nonExistentUser");

        assertFalse(result.isPresent());
    }
}
