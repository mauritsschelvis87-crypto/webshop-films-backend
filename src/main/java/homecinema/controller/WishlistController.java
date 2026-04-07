package homecinema.controller;

import homecinema.model.Film;
import homecinema.model.User;
import homecinema.repository.FilmRepository;
import homecinema.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "https://s1156856.student.inf.st.hsleiden.nl"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    public WishlistController(UserRepository userRepository, FilmRepository filmRepository) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
    }

    @GetMapping("/{userId}")
    public List<Film> getWishlist(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden!"));
        return user.getWishlist();
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

        return user.getWishlist();
    }

    @DeleteMapping("/{userId}/remove/{filmId}")
    public List<Film> removeFromWishlist(@PathVariable Long userId, @PathVariable Long filmId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getWishlist().removeIf(f -> f.getId().equals(filmId));
        userRepository.save(user);

        return user.getWishlist();
    }
}
