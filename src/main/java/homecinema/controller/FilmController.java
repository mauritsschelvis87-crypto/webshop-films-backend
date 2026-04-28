package homecinema.controller;

import homecinema.config.CorsOrigins;
import homecinema.model.Film;
import homecinema.service.FilmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = {CorsOrigins.LOCALHOST_4200, CorsOrigins.VERCEL_APP, CorsOrigins.SCHOOL_FRONTEND}, allowCredentials = "true")
@RestController
@RequestMapping("/api/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable Long id) {
        Film film = filmService.findById(id);
        if (film == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(film);
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        return filmService.save(film);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable Long id, @RequestBody Film filmDetails) {
        Film film = filmService.findById(id);
        if (film == null) {
            return ResponseEntity.notFound().build();
        }

        // Update fields
        film.setTitle(filmDetails.getTitle());
        film.setGenre(filmDetails.getGenre());
        film.setDirector(filmDetails.getDirector());
        film.setCountry(filmDetails.getCountry());
        film.setRegion(filmDetails.getRegion());
        film.setYear(filmDetails.getYear());
        film.setType(filmDetails.getType());
        film.setPrice(filmDetails.getPrice());
        film.setImageUrl(filmDetails.getImageUrl());
        film.setTrailerUrl(filmDetails.getTrailerUrl());
        film.setDescription(filmDetails.getDescription());
        film.setActors(filmDetails.getActors());

        Film updatedFilm = filmService.save(film);
        return ResponseEntity.ok(updatedFilm);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Long id) {
        filmService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
