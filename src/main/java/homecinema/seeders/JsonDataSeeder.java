package homecinema.seeders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import homecinema.model.Actor;
import homecinema.model.Brand;
import homecinema.model.Director;
import homecinema.model.DirectorPerson;
import homecinema.model.Film;
import homecinema.model.enums.FilmRegion;
import homecinema.repository.ActorRepository;
import homecinema.repository.BrandRepository;
import homecinema.repository.DirectorRepository;
import homecinema.repository.FilmRepository;
import homecinema.service.CloudinaryAssetResolver;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    private final DirectorRepository directorRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryAssetResolver cloudinaryAssetResolver;

    public JsonDataSeeder(FilmRepository filmRepository,
                          BrandRepository brandRepository,
                          ActorRepository actorRepository,
                          DirectorRepository directorRepository,
                          ObjectMapper objectMapper,
                          CloudinaryAssetResolver cloudinaryAssetResolver) {
        this.filmRepository = filmRepository;
        this.brandRepository = brandRepository;
        this.actorRepository = actorRepository;
        this.directorRepository = directorRepository;
        this.objectMapper = objectMapper;
        this.cloudinaryAssetResolver = cloudinaryAssetResolver;
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
            film.setRegion(determineRegion(brand, fj.getType()));
            film.setYear(fj.getYear());
            film.setRuntime(fj.getRuntime());
            film.setType(fj.getType());
            film.setPrice(fj.getPrice());
            film.setImageUrl(cloudinaryAssetResolver.resolveCover(fj.getImageUrl()));
            film.setTrailerUrl(fj.getTrailerUrl());
            film.setAspectRatio(fj.getAspectRatio());
            film.setColorOrBlackAndWhite(fj.getColorOrBlackAndWhite());
            film.setDescription(fj.getDescription());
            film.setBrand(brand);
            film.setActors(actors);
            film.setWeight(fj.getWeight());
            film.setStills(resolveStills(fj.getStills()));

            film.setSilent(fj.getSilent() != null ? fj.getSilent() : false);

            Film savedFilm = filmRepository.save(film);
            filmsByTitle.put(fj.getTitle(), new ArrayList<>(List.of(savedFilm)));
        }

        System.out.println("Seed data geladen!");
        seedDirectors();
    }

    private void seedDirectors() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/directors.json");
        if (inputStream == null) {
            System.err.println("Kon directors niet vinden!");
            return;
        }

        List<DirectorJson> directorsJson = objectMapper.readValue(inputStream, new TypeReference<>() {});
        Map<String, Director> directorsBySlug = directorRepository.findBySlugIn(extractDirectorSlugs(directorsJson)).stream()
                .collect(Collectors.toMap(Director::getSlug, director -> director));

        for (DirectorJson dj : directorsJson) {
            Director director = directorsBySlug.getOrDefault(dj.getSlug(), new Director());
            director.setSlug(dj.getSlug());
            director.setName(dj.getName());
            director.setBirthPlace(dj.getBirthPlace());
            director.setBirthYear(dj.getBirthYear());
            director.setDeathYear(dj.getDeathYear());
            director.setInfoLine(dj.getInfoLine());
            director.setBornLine(dj.getBornLine());
            director.setDiedLine(dj.getDiedLine());
            director.setPeople(toDirectorPeople(dj.getPeople()));
            director.setImage(cloudinaryAssetResolver.resolveDirector(dj.getImage()));
            director.setBio(dj.getBio());
            director.setEducation(normalizeEducation(dj.getEducation()));

            Director savedDirector = directorRepository.save(director);
            directorsBySlug.put(savedDirector.getSlug(), savedDirector);
        }
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

    private Set<String> extractDirectorSlugs(List<DirectorJson> directorsJson) {
        return directorsJson.stream()
                .map(DirectorJson::getSlug)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String normalizeEducation(JsonNode educationNode) {
        if (educationNode == null || educationNode.isNull()) {
            return null;
        }

        if (educationNode.isArray()) {
            List<String> educations = new ArrayList<>();
            educationNode.forEach(node -> {
                if (node.isTextual()) {
                    educations.add(node.asText());
                }
            });
            return String.join("; ", educations);
        }

        return educationNode.asText();
    }

    private List<DirectorPerson> toDirectorPeople(List<DirectorPersonJson> peopleJson) {
        if (peopleJson == null) {
            return null;
        }

        return peopleJson.stream().map(personJson -> {
            DirectorPerson person = new DirectorPerson();
            person.setName(personJson.getName());
            person.setBirthDate(personJson.getBirthDate());
            person.setBirthPlace(personJson.getBirthPlace());
            person.setBirthYear(personJson.getBirthYear());
            person.setDeathDate(personJson.getDeathDate());
            person.setDeathYear(personJson.getDeathYear());
            return person;
        }).toList();
    }

    private List<String> resolveStills(List<String> stills) {
        if (stills == null) {
            return List.of();
        }

        return stills.stream()
                .map(cloudinaryAssetResolver::resolveStill)
                .filter(StringUtils::hasText)
                .toList();
    }

    private FilmRegion determineRegion(Brand brand, String type) {
        if (isRegionFree(type)) {
            return FilmRegion.FREE;
        }

        if (brand == null || brand.getName() == null) {
            throw new IllegalArgumentException("Film brand is required to determine region.");
        }

        return switch (brand.getName()) {
            case "Criterion Collection" -> FilmRegion.A;
            case "Masters of Cinema", "BFI" -> FilmRegion.B;
            default -> throw new IllegalArgumentException("Unsupported brand for region mapping: " + brand.getName());
        };
    }

    private boolean isRegionFree(String type) {
        return "4K Ultra HD".equalsIgnoreCase(type);
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

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DirectorJson {
        private String slug;
        private String name;
        private String birthPlace;
        private Integer birthYear;
        private Integer deathYear;
        private String infoLine;
        private String bornLine;
        private String diedLine;
        private List<DirectorPersonJson> people;
        private String image;
        private String bio;
        private JsonNode education;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DirectorPersonJson {
        private String name;
        private String birthDate;
        private String birthPlace;
        private Integer birthYear;
        private String deathDate;
        private Integer deathYear;
    }
}
