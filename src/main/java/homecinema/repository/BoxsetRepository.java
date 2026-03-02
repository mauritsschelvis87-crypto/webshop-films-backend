package homecinema.repository;

import homecinema.model.Boxset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxsetRepository extends JpaRepository<Boxset, Long> {
}
