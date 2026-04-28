package homecinema.service;

import homecinema.model.Film;
import homecinema.model.User;
import homecinema.model.enums.FilmRegion;
import homecinema.repository.FilmRepository;
import homecinema.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public FilmService(FilmRepository filmRepository, UserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    public List<Film> findAll() {
        List<Film> films = filmRepository.findAll();
        enrichWithCommunityRatings(films);
        return films;
    }

    public Film findById(Long id) {
        Film film = filmRepository.findById(id).orElse(null);
        if (film != null) {
            enrichWithCommunityRatings(List.of(film));
        }
        return film;
    }

    public List<Film> searchByTitle(String title) {
        List<Film> films = filmRepository.findByTitleContainingIgnoreCase(title);
        enrichWithCommunityRatings(films);
        return films;
    }

    public Film save(Film film) {
        applyRegionRules(film);
        return filmRepository.save(film);
    }

    public void delete(Long id) {
        filmRepository.deleteById(id);
    }

    private void applyRegionRules(Film film) {
        if (film != null && "4K Ultra HD".equalsIgnoreCase(film.getType())) {
            film.setRegion(FilmRegion.FREE);
        }
    }

    public void enrichWithCommunityRatings(List<Film> films) {
        if (films == null || films.isEmpty()) return;

        List<User> allUsers = userRepository.findAll();
        Map<Long, List<BigDecimal>> ratingsPerFilm = new HashMap<>();

        for (User user : allUsers) {
            user.getFilmRatings().forEach((filmId, rating) -> {
                ratingsPerFilm.computeIfAbsent(filmId, k -> new java.util.ArrayList<>()).add(rating);
            });
        }

        for (Film film : films) {
            List<BigDecimal> ratings = ratingsPerFilm.get(film.getId());
            if (ratings != null && !ratings.isEmpty()) {
                BigDecimal sum = ratings.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal average = sum.divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
                
                // Round to nearest 0.5 as requested
                BigDecimal roundedAverage = roundToHalf(average);
                
                film.setAverageCommunityRating(roundedAverage);
                film.setCommunityRatingCount(ratings.size());
            } else {
                film.setAverageCommunityRating(BigDecimal.ZERO);
                film.setCommunityRatingCount(0);
            }
        }
    }

    private BigDecimal roundToHalf(BigDecimal value) {
        return value.multiply(new BigDecimal("2"))
                .setScale(0, RoundingMode.HALF_UP)
                .divide(new BigDecimal("2"), 1, RoundingMode.HALF_UP);
    }

}
