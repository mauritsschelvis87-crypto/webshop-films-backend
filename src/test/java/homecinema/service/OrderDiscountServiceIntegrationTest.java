package homecinema.service;

import homecinema.dto.OrderRequestDTO;
import homecinema.dto.OrderResponseDTO;
import homecinema.model.Brand;
import homecinema.model.Film;
import homecinema.model.User;
import homecinema.repository.BrandRepository;
import homecinema.repository.FilmRepository;
import homecinema.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderDiscountServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void giftCardCodeAppliesFixedDiscount() {
        User user = saveUser("giftcard-user", "giftcard@example.com");
        Film film = saveFilm("Cure", "Blu-ray", 24.99);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsername(user.getUsername());
        request.setGiftCardCode("FREE10");
        request.setItems(List.of(orderItem(film.getId(), 1)));

        OrderResponseDTO order = orderService.createOrderFromDto(request);

        assertEquals(24.99, order.getSubtotalPrice(), 0.001);
        assertEquals(10.00, order.getDiscountAmount(), 0.001);
        assertEquals(14.99, order.getTotalPrice(), 0.001);
        assertEquals("FREE10", order.getAppliedGiftCardCode());
    }

    @Test
    void giftCodeAppliesPercentageDiscountOverSubtotal() {
        User user = saveUser("giftcode-user", "giftcode@example.com");
        Film firstFilm = saveFilm("The Third Man", "Blu-ray", 20.00);
        Film secondFilm = saveFilm("Le Samourai", "4K", 30.00);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsername(user.getUsername());
        request.setGiftCode("FREE10");
        request.setItems(List.of(
                orderItem(firstFilm.getId(), 1),
                orderItem(secondFilm.getId(), 1)
        ));

        OrderResponseDTO order = orderService.createOrderFromDto(request);

        assertEquals(50.00, order.getSubtotalPrice(), 0.001);
        assertEquals(5.00, order.getDiscountAmount(), 0.001);
        assertEquals(45.00, order.getTotalPrice(), 0.001);
        assertEquals("FREE10", order.getAppliedGiftCode());
    }

    @Test
    void previewCalculatesCombinedDiscountsWithoutSideEffects() {
        User user = saveUser("preview-user", "preview@example.com");
        Film film = saveFilm("Late Spring", "Blu-ray", 21.99);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsername(user.getUsername());
        request.setGiftCardCode("FREE10");
        request.setGiftCode("FREE5");
        request.setItems(List.of(orderItem(film.getId(), 1)));

        OrderResponseDTO preview = orderService.previewOrderFromDto(request);

        assertEquals("PREVIEW", preview.getStatus());
        assertEquals(21.99, preview.getSubtotalPrice(), 0.001);
        assertEquals(11.0995, preview.getDiscountAmount(), 0.001);
        assertEquals(10.8905, preview.getTotalPrice(), 0.001);
        assertEquals("FREE10", preview.getAppliedGiftCardCode());
        assertEquals("FREE5", preview.getAppliedGiftCode());
    }

    @Test
    void invalidGiftCardCodeThrowsExpectedError() {
        User user = saveUser("invalid-card-user", "invalid-card@example.com");
        Film film = saveFilm("Floating Weeds", "Blu-ray", 15.00);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsername(user.getUsername());
        request.setGiftCardCode("NOPE");
        request.setItems(List.of(orderItem(film.getId(), 1)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.previewOrderFromDto(request));
        assertEquals("The code is invalid.", exception.getMessage());
    }

    @Test
    void emailAsUsernameIsAcceptedForPreview() {
        User user = saveUser("email-user", "email-user@example.com");
        Film film = saveFilm("An Autumn Afternoon", "Blu-ray", 12.00);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsername(user.getEmail());
        request.setGiftCardCode("FREE10");
        request.setItems(List.of(orderItem(film.getId(), 1)));

        OrderResponseDTO preview = orderService.previewOrderFromDto(request);

        assertEquals(12.00, preview.getSubtotalPrice(), 0.001);
        assertEquals(10.00, preview.getDiscountAmount(), 0.001);
        assertEquals(2.00, preview.getTotalPrice(), 0.001);
    }

    @Test
    void discountNeverExceedsSubtotal() {
        User user = saveUser("max-discount-user", "max-discount@example.com");
        Film film = saveFilm("Early Summer", "Blu-ray", 8.00);

        OrderRequestDTO request = new OrderRequestDTO();
        request.setUsername(user.getUsername());
        request.setGiftCardCode("FREE30");
        request.setGiftCode("FREE10");
        request.setItems(List.of(orderItem(film.getId(), 1)));

        OrderResponseDTO preview = orderService.previewOrderFromDto(request);

        assertEquals(8.00, preview.getSubtotalPrice(), 0.001);
        assertEquals(8.00, preview.getDiscountAmount(), 0.001);
        assertEquals(0.0, preview.getTotalPrice(), 0.001);
    }

    private User saveUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("secret");
        user.setRole("USER");
        user.setName(username);
        return userRepository.save(user);
    }

    private Film saveFilm(String title, String type, double price) {
        Brand brand = brandRepository.save(new Brand("Test Brand " + title));
        Film film = new Film();
        film.setTitle(title);
        film.setGenre("Drama");
        film.setDirector("Director");
        film.setCountry("Country");
        film.setYear(1960);
        film.setRuntime(100);
        film.setType(type);
        film.setPrice(price);
        film.setImageUrl("image");
        film.setTrailerUrl("trailer");
        film.setAspectRatio("1.85:1");
        film.setColorOrBlackAndWhite("Black and White");
        film.setSilent(false);
        film.setWeight(200);
        film.setDescription("Description");
        film.setBrand(brand);
        return filmRepository.save(film);
    }

    private OrderRequestDTO.OrderItemDTO orderItem(Long productId, int quantity) {
        OrderRequestDTO.OrderItemDTO item = new OrderRequestDTO.OrderItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }
}
