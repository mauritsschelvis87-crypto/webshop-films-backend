package homecinema.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import homecinema.model.Brand;
import homecinema.model.Film;
import homecinema.repository.BrandRepository;
import homecinema.repository.FilmRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void previewAndPlaceOrderWorkForAuthenticatedCartFlow() throws Exception {
        String email = "cart-flow@example.com";
        String password = "Test123!";
        Film film = saveFilm("Harakiri", 30.00);
        String token = registerAndLogin(email, password);

        String payload = """
                {
                  "username": "%s",
                  "giftCardCode": "FREE10",
                  "giftCode": "FREE5",
                  "items": [
                    { "productId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(email, film.getId());

        mockMvc.perform(post("/api/orders/preview")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREVIEW"))
                .andExpect(jsonPath("$.subtotalPrice").value(30.0))
                .andExpect(jsonPath("$.discountAmount").value(11.5))
                .andExpect(jsonPath("$.totalPrice").value(18.5))
                .andExpect(jsonPath("$.appliedGiftCardCode").value("FREE10"))
                .andExpect(jsonPath("$.appliedGiftCode").value("FREE5"))
                .andExpect(jsonPath("$.orderItems[0].film.title").value("Harakiri"));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.subtotalPrice").value(30.0))
                .andExpect(jsonPath("$.discountAmount").value(11.5))
                .andExpect(jsonPath("$.totalPrice").value(18.5))
                .andExpect(jsonPath("$.appliedGiftCardCode").value("FREE10"))
                .andExpect(jsonPath("$.appliedGiftCode").value("FREE5"))
                .andExpect(jsonPath("$.number").isString());
    }

    @Test
    void previewReturnsBadRequestForInvalidDiscountCode() throws Exception {
        String email = "cart-invalid@example.com";
        String password = "Test123!";
        Film film = saveFilm("Floating Weeds", 20.00);
        String token = registerAndLogin(email, password);

        String payload = """
                {
                  "username": "%s",
                  "giftCode": "NOPE",
                  "items": [
                    { "productId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(email, film.getId());

        mockMvc.perform(post("/api/orders/preview")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The code is invalid."));
    }

    private String registerAndLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        return jsonNode.get("token").asText();
    }

    private Film saveFilm(String title, double price) {
        Brand brand = brandRepository.save(new Brand("Brand " + title));
        Film film = new Film();
        film.setTitle(title);
        film.setGenre("Drama");
        film.setDirector("Director");
        film.setCountry("Japan");
        film.setYear(1962);
        film.setRuntime(133);
        film.setType("Blu-ray");
        film.setPrice(price);
        film.setImageUrl("image");
        film.setTrailerUrl("trailer");
        film.setAspectRatio("2.35:1");
        film.setColorOrBlackAndWhite("Black and White");
        film.setSilent(false);
        film.setWeight(200);
        film.setDescription("Description");
        film.setBrand(brand);
        return filmRepository.save(film);
    }
}
