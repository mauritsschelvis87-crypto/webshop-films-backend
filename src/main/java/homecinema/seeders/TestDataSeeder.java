package homecinema.seeders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import homecinema.model.Film;
import homecinema.model.User;
import homecinema.repository.FilmRepository;
import homecinema.repository.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class TestDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public TestDataSeeder(UserRepository userRepository,
                          FilmRepository filmRepository,
                          PasswordEncoder passwordEncoder,
                          ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/seed-users.json");
        if (inputStream == null) {
            System.err.println("Could not find seed-users.json!");
            return;
        }

        List<UserJson> usersJson = objectMapper.readValue(inputStream, new TypeReference<>() {});
        List<Film> allFilms = filmRepository.findAll();

        if (allFilms.size() < 50) {
            System.err.println("Not enough films in the database to seed collections (need at least 50).");
            return;
        }

        for (UserJson uj : usersJson) {
            User user = userRepository.findByUsername(uj.getUsername())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(uj.getUsername());
                        newUser.setEmail(uj.getEmail());
                        newUser.setPassword(passwordEncoder.encode(uj.getPassword()));
                        newUser.setRole("ROLE_USER");
                        newUser.setName(uj.getUsername());
                        return userRepository.save(newUser);
                    });

            seedCollectionAndRatings(user, allFilms);
        }

        System.out.println("Test data seeding completed!");
    }

    private void seedCollectionAndRatings(User user, List<Film> allFilms) {
        List<Film> wishlist = user.getWishlist();
        
        // If user already has enough items, we skip
        if (wishlist.size() >= 50) {
            return;
        }

        // Shuffle a copy of all films and pick 50
        List<Film> shuffledFilms = new java.util.ArrayList<>(allFilms);
        Collections.shuffle(shuffledFilms);
        List<Film> selectedFilms = shuffledFilms.stream()
                .limit(50)
                .collect(Collectors.toList());

        for (Film film : selectedFilms) {
            // Check if film is already in wishlist
            if (wishlist.stream().noneMatch(f -> f.getId().equals(film.getId()))) {
                wishlist.add(film);
            }

            // Check if rating already exists
            if (!user.getFilmRatings().containsKey(film.getId())) {
                // Generate random rating between 1.0 and 5.0 with 0.5 steps
                double ratingValue = 1.0 + (random.nextInt(9) * 0.5); 
                user.getFilmRatings().put(film.getId(), BigDecimal.valueOf(ratingValue).setScale(1, RoundingMode.HALF_UP));
            }
        }

        userRepository.save(user);
    }

    @Getter
    @Setter
    public static class UserJson {
        private String username;
        private String email;
        private String password;
    }
}
