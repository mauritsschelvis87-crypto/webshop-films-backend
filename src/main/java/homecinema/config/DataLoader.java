package homecinema.config;

import homecinema.repository.ActorRepository;
import homecinema.repository.FilmRepository;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final FilmRepository filmRepository;
    private final ActorRepository actorRepository;

    public DataLoader(FilmRepository filmRepository, ActorRepository actorRepository) {
        this.filmRepository = filmRepository;
        this.actorRepository = actorRepository;
    }
}
