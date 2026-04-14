package homecinema.api;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FilmApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void filmsEndpointReturnsProductsForFrontendCartAndListing() throws Exception {
        Film film = saveFilm("Tokyo Story", 19.99);

        ResultActions filmListResponse = mockMvc.perform(get("/api/films")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(content().string(containsString("Tokyo Story")));

        mockMvc.perform(get("/api/films/{id}", film.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(film.getId()))
                .andExpect(jsonPath("$.title").value("Tokyo Story"))
                .andExpect(jsonPath("$.price").value(19.99));
    }

    private Film saveFilm(String title, double price) {
        Brand brand = brandRepository.save(new Brand("Brand " + title));
        Film film = new Film();
        film.setTitle(title);
        film.setGenre("Drama");
        film.setDirector("Director");
        film.setCountry("Japan");
        film.setYear(1953);
        film.setRuntime(136);
        film.setType("Blu-ray");
        film.setPrice(price);
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
}
