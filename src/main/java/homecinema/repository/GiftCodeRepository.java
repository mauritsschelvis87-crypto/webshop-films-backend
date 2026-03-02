package homecinema.repository;

import homecinema.model.GiftCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiftCodeRepository extends JpaRepository<GiftCode, Long> {
    Optional<GiftCode> findByCode(String code);
}
