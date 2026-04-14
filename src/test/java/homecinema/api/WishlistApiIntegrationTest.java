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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WishlistApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void wishlistFilmCanOnlyBeRatedWhenPresentInCollection() throws Exception {
        Film film = saveFilm("Late Spring");
        AuthSession session = registerAndLogin("wishlist-rating@example.com", "Test123!");

        mockMvc.perform(put("/api/wishlist/{userId}/rating/{filmId}", session.userId(), film.getId())
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/wishlist/{userId}/add/{filmId}", session.userId(), film.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(film.getId()))
                .andExpect(jsonPath("$[0].userRating").doesNotExist());

        mockMvc.perform(put("/api/wishlist/{userId}/rating/{filmId}", session.userId(), film.getId())
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(film.getId()))
                .andExpect(jsonPath("$[0].userRating").value(5));

        mockMvc.perform(get("/api/wishlist/{userId}", session.userId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(film.getId()))
                .andExpect(jsonPath("$[0].userRating").value(5));

        mockMvc.perform(delete("/api/wishlist/{userId}/remove/{filmId}", session.userId(), film.getId())
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private AuthSession registerAndLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk());

        String token = extractToken(mockMvc.perform(post("/api/auth/login")
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
                .getContentAsString());

        String accountResponse = mockMvc.perform(get("/api/account/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode accountJson = objectMapper.readTree(accountResponse);
        return new AuthSession(accountJson.get("id").asLong(), token);
    }

    private String extractToken(String responseBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("token").asText();
    }

    private Film saveFilm(String title) {
        Brand brand = brandRepository.save(new Brand("Brand " + title));
        Film film = new Film();
        film.setTitle(title);
        film.setGenre("Drama");
        film.setDirector("Director");
        film.setCountry("Japan");
        film.setYear(1949);
        film.setRuntime(108);
        film.setType("Blu-ray");
        film.setPrice(19.99);
        film.setImageUrl("image");
        film.setTrailerUrl("trailer");
        film.setAspectRatio("1.37:1");
        film.setColorOrBlackAndWhite("Black and White");
        film.setSilent(false);
        film.setWeight(200);
        film.setDescription("Description");
        film.setBrand(brand);
        return filmRepository.save(film);
    }

    private record AuthSession(Long userId, String token) {
    }
}
