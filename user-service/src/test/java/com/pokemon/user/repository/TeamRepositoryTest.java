package com.pokemon.user.repository;

import com.pokemon.user.domain.Team;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TeamRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;

    User savedUser;

    @BeforeEach
    void setUp() {
        teamRepository.deleteAll();
        userRepository.deleteAll();

        User u = new User();
        u.setUsername("ash");
        u.setEmail("ash@pallet.com");
        savedUser = userRepository.save(u);
    }

    @Test
    void savesTeam_findsAllByUser() {
        Team t = new Team();
        t.setUser(savedUser);
        t.setName("My Team");
        t.setFormat("GEN9OU");
        t.setPokemonData("[\"garchomp\"]");
        teamRepository.save(t);

        List<Team> teams = teamRepository.findAllByUser(savedUser);
        assertThat(teams).hasSize(1);
        assertThat(teams.get(0).getName()).isEqualTo("My Team");
    }

    @Test
    void findByIdAndUser_doesNotLeakOtherUsersTeam() {
        // create another user and their team
        User other = new User();
        other.setUsername("misty");
        other.setEmail("misty@cerulean.com");
        User savedOther = userRepository.save(other);

        Team t = new Team();
        t.setUser(savedOther);
        t.setName("Misty Team");
        t.setFormat("GEN9OU");
        t.setPokemonData("[\"starmie\"]");
        Team savedTeam = teamRepository.save(t);

        // ash should not see misty's team
        Optional<Team> found = teamRepository.findByIdAndUser(savedTeam.getId(), savedUser);
        assertThat(found).isEmpty();
    }
}
