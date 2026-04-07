package homecinema.seeders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import homecinema.model.Actor;
import homecinema.model.Brand;
import homecinema.model.Film;
import homecinema.repository.ActorRepository;
import homecinema.repository.BrandRepository;
import homecinema.repository.FilmRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JsonDataSeeder implements CommandLineRunner {

    private final FilmRepository filmRepository;
    private final BrandRepository brandRepository;
    private final ActorRepository actorRepository;
    private final ObjectMapper objectMapper;

    public JsonDataSeeder(FilmRepository filmRepository,
                          BrandRepository brandRepository,
                          ActorRepository actorRepository) {
        this.filmRepository = filmRepository;
        this.brandRepository = brandRepository;
        this.actorRepository = actorRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/films.json");
        if (inputStream == null) {
            System.err.println("Kon films niet vinden!");
            return;
        }

        List<FilmJson> filmsJson = objectMapper.readValue(inputStream, new TypeReference<>() {});

        for (FilmJson fj : filmsJson) {
            Brand brand = brandRepository.findByName(fj.getBrand().getName())
                    .orElseGet(() -> brandRepository.save(new Brand(fj.getBrand().getName())));

            Set<Actor> actors = fj.getActors().stream()
                    .map(aJson -> actorRepository.findByName(aJson.getName())
                            .orElseGet(() -> actorRepository.save(new Actor(aJson.getName()))))
                    .collect(Collectors.toSet());

            List<Film> existingFilms = new ArrayList<>(filmRepository.findAllByTitle(fj.getTitle()));
            Film film = existingFilms.isEmpty() ? new Film() : existingFilms.get(0);

            if (existingFilms.size() > 1) {
                filmRepository.deleteAll(existingFilms.subList(1, existingFilms.size()));
            }

            film.setTitle(fj.getTitle());
            film.setGenre(fj.getGenre());
            film.setDirector(fj.getDirector());
            film.setCountry(fj.getCountry());
            film.setYear(fj.getYear());
            film.setRuntime(fj.getRuntime());
            film.setType(fj.getType());
            film.setPrice(fj.getPrice());
            film.setImageUrl(fj.getImageUrl());
            film.setTrailerUrl(fj.getTrailerUrl());
            film.setAspectRatio(fj.getAspectRatio());
            film.setColorOrBlackAndWhite(fj.getColorOrBlackAndWhite());
            film.setDescription(fj.getDescription());
            film.setBrand(brand);
            film.setActors(actors);
            film.setWeight(fj.getWeight());
            film.setStills(fj.getStills());

            film.setSilent(fj.getSilent() != null ? fj.getSilent() : false);

            filmRepository.save(film);
        }

        System.out.println("Seed data geladen!");
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FilmJson {
        private String title;
        private String genre;
        private String director;
        private String country;
        private int year;
        private int runtime;
        private String type;
        private double price;
        private String imageUrl;
        private String trailerUrl;
        private String aspectRatio;
        private String colorOrBlackAndWhite;
        private String description;
        private BrandJson brand;
        private Set<ActorJson> actors;
        private List<String> stills;
        private int weight;
        private Boolean silent;
        private Boolean technicolor;
    }

    @Getter
    @Setter
    public static class BrandJson {
        private String name;
    }

    @Getter
    @Setter
    public static class ActorJson {
        private String name;
    }
}
