package homecinema.repository;

import homecinema.model.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {

    List<Director> findBySlugIn(Collection<String> slugs);

    Optional<Director> findBySlug(String slug);
}
