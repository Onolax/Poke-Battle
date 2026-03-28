package com.pokemon.user.repository;

import com.pokemon.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void savesUser_findsById() {
        User u = new User();
        u.setUsername("ash");
        u.setEmail("ash@pallet.com");
        u.setPasswordHash("hashed");

        User saved = repository.save(u);

        Optional<User> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("ash");
        assertThat(found.get().getEmail()).isEqualTo("ash@pallet.com");
    }

    @Test
    void findByUsername_returnsUser() {
        User u = new User();
        u.setUsername("misty");
        u.setEmail("misty@cerulean.com");
        repository.save(u);

        Optional<User> found = repository.findByUsername("misty");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("misty@cerulean.com");
    }

    @Test
    void duplicateUsername_throwsException() {
        User u1 = new User();
        u1.setUsername("brock");
        u1.setEmail("brock@pewter.com");
        repository.save(u1);

        User u2 = new User();
        u2.setUsername("brock");
        u2.setEmail("brock2@pewter.com");

        assertThatThrownBy(() -> {
            repository.saveAndFlush(u2);
        }).isInstanceOf(Exception.class);
    }
}
