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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        Map<String, Brand> brandsByName = loadBrandsByName(filmsJson);
        Map<String, Actor> actorsByName = loadActorsByName(filmsJson);
        Map<String, List<Film>> filmsByTitle = filmRepository.findAllByTitleIn(extractTitles(filmsJson)).stream()
                .collect(Collectors.groupingBy(Film::getTitle));

        for (FilmJson fj : filmsJson) {
            Brand brand = brandsByName.get(fj.getBrand().getName());

            Set<Actor> actors = fj.getActors().stream()
                    .map(aJson -> actorsByName.get(aJson.getName()))
                    .collect(Collectors.toSet());

            List<Film> existingFilms = new ArrayList<>(filmsByTitle.getOrDefault(fj.getTitle(), List.of()));
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

            Film savedFilm = filmRepository.save(film);
            filmsByTitle.put(fj.getTitle(), new ArrayList<>(List.of(savedFilm)));
        }

        System.out.println("Seed data geladen!");
    }

    private Map<String, Brand> loadBrandsByName(List<FilmJson> filmsJson) {
        Set<String> brandNames = filmsJson.stream()
                .map(FilmJson::getBrand)
                .map(BrandJson::getName)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, Brand> brandsByName = brandRepository.findByNameIn(brandNames).stream()
                .collect(Collectors.toMap(Brand::getName, brand -> brand));

        List<Brand> newBrands = brandNames.stream()
                .filter(name -> !brandsByName.containsKey(name))
                .map(Brand::new)
                .toList();

        for (Brand brand : brandRepository.saveAll(newBrands)) {
            brandsByName.put(brand.getName(), brand);
        }

        return brandsByName;
    }

    private Map<String, Actor> loadActorsByName(List<FilmJson> filmsJson) {
        Set<String> actorNames = filmsJson.stream()
                .map(FilmJson::getActors)
                .filter(actors -> actors != null)
                .flatMap(Collection::stream)
                .map(ActorJson::getName)
                .collect(Collectors.toCollection(HashSet::new));

        Map<String, Actor> actorsByName = actorRepository.findByNameIn(actorNames).stream()
                .collect(Collectors.toMap(Actor::getName, actor -> actor));

        List<Actor> newActors = actorNames.stream()
                .filter(name -> !actorsByName.containsKey(name))
                .map(Actor::new)
                .toList();

        for (Actor actor : actorRepository.saveAll(newActors)) {
            actorsByName.put(actor.getName(), actor);
        }

        return actorsByName;
    }

    private Set<String> extractTitles(List<FilmJson> filmsJson) {
        return filmsJson.stream()
                .map(FilmJson::getTitle)
                .collect(Collectors.toCollection(HashSet::new));
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
