package homecinema.controller;

import homecinema.dto.FilmRatingRequest;
import homecinema.model.Film;
import homecinema.model.User;
import homecinema.repository.FilmRepository;
import homecinema.repository.UserRepository;
import homecinema.service.FilmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "https://s1156856.student.inf.st.hsleiden.nl"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private static final BigDecimal MIN_RATING = new BigDecimal("0.5");
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");
    private static final BigDecimal HALF_STAR_STEP = new BigDecimal("0.5");

    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final FilmService filmService;

    public WishlistController(UserRepository userRepository, FilmRepository filmRepository, FilmService filmService) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.filmService = filmService;
    }

    @GetMapping("/{userId}")
    public List<Film> getWishlist(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden!"));
        return enrichWishlistWithRatings(user);
    }

    @PostMapping("/{userId}/add/{filmId}")
    public List<Film> addToWishlist(@PathVariable Long userId, @PathVariable Long filmId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden!"));
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film niet gevonden!"));

        if (!user.getWishlist().contains(film)) {
            user.getWishlist().add(film);
            userRepository.save(user);
        }

        return enrichWishlistWithRatings(user);
    }

    @DeleteMapping("/{userId}/remove/{filmId}")
    public List<Film> removeFromWishlist(@PathVariable Long userId, @PathVariable Long filmId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getWishlist().removeIf(f -> f.getId().equals(filmId));
        user.getFilmRatings().remove(filmId);
        userRepository.save(user);

        return enrichWishlistWithRatings(user);
    }

    @PutMapping("/{userId}/rating/{filmId}")
    public ResponseEntity<List<Film>> rateWishlistFilm(@PathVariable Long userId,
                                                       @PathVariable Long filmId,
                                                       @RequestBody FilmRatingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden!"));

        Film film = user.getWishlist().stream()
                .filter(item -> item.getId().equals(filmId))
                .findFirst()
                .orElse(null);

        if (film == null) {
            return ResponseEntity.badRequest().build();
        }

        BigDecimal rating = request.getRating();
        if (!isValidRating(rating)) {
            return ResponseEntity.badRequest().build();
        }

        user.getFilmRatings().put(filmId, rating);
        userRepository.save(user);
        return ResponseEntity.ok(enrichWishlistWithRatings(user));
    }

    private List<Film> enrichWishlistWithRatings(User user) {
        user.getWishlist().forEach(film -> film.setUserRating(user.getFilmRatings().get(film.getId())));
        filmService.enrichWithCommunityRatings(user.getWishlist());
        return user.getWishlist();
    }

    private boolean isValidRating(BigDecimal rating) {
        if (rating == null || rating.compareTo(MIN_RATING) < 0 || rating.compareTo(MAX_RATING) > 0) {
            return false;
        }

        return rating.remainder(HALF_STAR_STEP).compareTo(BigDecimal.ZERO) == 0;
    }
}
