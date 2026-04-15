package homecinema.service;

import homecinema.model.Film;
import homecinema.model.enums.FilmRegion;
import homecinema.repository.FilmRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilmService {
    private final FilmRepository filmRepository;

    public FilmService(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    public List<Film> findAll() {
        return filmRepository.findAll();
    }

    public Film findById(Long id) {
        return filmRepository.findById(id).orElse(null);
    }

    public List<Film> searchByTitle(String title) {
        return filmRepository.findByTitleContainingIgnoreCase(title);
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

}
