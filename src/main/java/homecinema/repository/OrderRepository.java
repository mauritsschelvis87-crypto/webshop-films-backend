package homecinema.repository;

import homecinema.model.Order;
import homecinema.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.film f " +
            "LEFT JOIN FETCH f.brand " +
            "LEFT JOIN FETCH oi.boxset b " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItemsAndFilmBrand(@Param("id") Long id);


}
