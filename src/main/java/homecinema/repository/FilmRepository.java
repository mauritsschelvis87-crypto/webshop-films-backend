package homecinema.repository;

import homecinema.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FilmRepository extends JpaRepository<Film, Long> {

    Optional<Film> findByTitle(String title); // exact zoeken, handig voor seeder

    List<Film> findAllByTitle(String title);
    List<Film> findAllByTitleIn(Collection<String> titles);

    List<Film> findByTitleContainingIgnoreCase(String title);

    List<Film> findByGenreContainingIgnoreCase(String genre);

    List<Film> findByDirectorContainingIgnoreCase(String director);

    List<Film> findByCountryContainingIgnoreCase(String country);

    List<Film> findByType(String type);
}
