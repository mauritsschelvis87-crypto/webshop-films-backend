package homecinema.service;

import homecinema.model.Director;
import homecinema.repository.DirectorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class DirectorService {
    private final DirectorRepository directorRepository;

    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    public List<Director> findAll() {
        return directorRepository.findAll();
    }

    public Director findBySlug(String slug) {
        return directorRepository.findBySlug(slug)
                .orElseGet(() -> directorRepository.findAll().stream()
                        .filter(director -> matchesSlug(director.getSlug(), slug))
                        .findFirst()
                        .orElse(null));
    }

    public Director save(Director director) {
        return directorRepository.save(director);
    }

    private boolean matchesSlug(String directorSlug, String routeSlug) {
        String normalizedDirector = normalize(directorSlug);
        String normalizedRoute = normalize(routeSlug);

        return normalizedDirector.equals(normalizedRoute)
                || normalizedDirector.replace("-w-", "-").equals(normalizedRoute)
                || normalizedRoute.replace("-w-", "-").equals(normalizedDirector);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
