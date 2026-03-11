package homecinema.repository;

import homecinema.model.Actor;
import homecinema.model.Brand;
import homecinema.model.Film;
import homecinema.model.Order;
import homecinema.model.OrderItem;
import homecinema.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class LazyLoadingRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void filmActorsAreLazyLoaded() {
        Actor actor = new Actor("Toshiro Mifune");
        entityManager.persist(actor);

        Brand brand = new Brand("Criterion");
        entityManager.persist(brand);

        Film film = new Film();
        film.setTitle("Seven Samurai");
        film.setGenre("Drama");
        film.setDirector("Akira Kurosawa");
        film.setCountry("Japan");
        film.setYear(1954);
        film.setRuntime(207);
        film.setType("Blu-ray");
        film.setPrice(24.99);
        film.setImageUrl("image");
        film.setTrailerUrl("trailer");
        film.setAspectRatio("1.37:1");
        film.setColorOrBlackAndWhite("Black and White");
        film.setSilent(false);
        film.setWeight(200);
        film.setDescription("Classic film");
        film.setBrand(brand);
        film.setActors(Set.of(actor));
        entityManager.persist(film);

        entityManager.flush();
        entityManager.clear();

        Film savedFilm = filmRepository.findById(film.getId()).orElseThrow();

        assertFalse(entityManagerFactory.getPersistenceUnitUtil().isLoaded(savedFilm, "actors"));

        assertEquals(1, savedFilm.getActors().size());
        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(savedFilm, "actors"));
    }

    @Test
    void findByIdWithItemsAndFilmBrandFetchesRequiredRelations() {
        User user = new User();
        user.setUsername("lazy-user");
        user.setEmail("lazy@example.com");
        user.setPassword("secret");
        user.setRole("USER");
        user.setName("Lazy User");
        entityManager.persist(user);

        Brand brand = new Brand("Arrow");
        entityManager.persist(brand);

        Film film = new Film();
        film.setTitle("Harakiri");
        film.setGenre("Drama");
        film.setDirector("Masaki Kobayashi");
        film.setCountry("Japan");
        film.setYear(1962);
        film.setRuntime(133);
        film.setType("Blu-ray");
        film.setPrice(19.99);
        film.setImageUrl("image");
        film.setTrailerUrl("trailer");
        film.setAspectRatio("2.35:1");
        film.setColorOrBlackAndWhite("Black and White");
        film.setSilent(false);
        film.setWeight(180);
        film.setDescription("Another classic film");
        film.setBrand(brand);
        entityManager.persist(film);

        Order order = new Order();
        order.setUser(user);
        order.setNumber("ORD-TEST1234");
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(19.99);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setFilm(film);
        item.setQuantity(1);
        item.setPrice(19.99);

        order.setOrderItems(List.of(item));
        entityManager.persist(order);

        entityManager.flush();
        entityManager.clear();

        Order savedOrder = orderRepository.findByIdWithItemsAndFilmBrand(order.getId()).orElseThrow();

        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(savedOrder, "orderItems"));
        assertEquals(1, savedOrder.getOrderItems().size());

        OrderItem savedItem = savedOrder.getOrderItems().get(0);
        assertNotNull(savedItem.getFilm());
        assertEquals("Harakiri", savedItem.getFilm().getTitle());
        assertNotNull(savedItem.getFilm().getBrand());
        assertEquals("Arrow", savedItem.getFilm().getBrand().getName());
    }
}
