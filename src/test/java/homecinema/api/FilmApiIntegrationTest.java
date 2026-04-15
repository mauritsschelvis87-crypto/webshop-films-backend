package homecinema.api;

import homecinema.model.Brand;
import homecinema.model.Film;
import homecinema.model.enums.FilmRegion;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.region").value("B"))
                .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    void filmsEndpointReturnsRegionAForCriterionTitles() throws Exception {
        Film film = saveFilm("Late Spring", 17.99, "Criterion Collection", FilmRegion.A);

        mockMvc.perform(get("/api/films/{id}", film.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("A"));
    }

    @Test
    void creatingFourKUltraHdFilmForcesRegionFree() throws Exception {
        Brand brand = brandRepository.save(new Brand("Criterion Collection"));

        mockMvc.perform(post("/api/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "The Third Man",
                                  "genre": "Noir",
                                  "director": "Carol Reed",
                                  "country": "UK",
                                  "region": "B",
                                  "year": 1949,
                                  "runtime": 104,
                                  "type": "4K Ultra HD",
                                  "price": 29.99,
                                  "imageUrl": "image",
                                  "trailerUrl": "trailer",
                                  "aspectRatio": "1.37:1",
                                  "colorOrBlackAndWhite": "Black and White",
                                  "silent": false,
                                  "weight": 200,
                                  "description": "Description",
                                  "brand": { "id": %d }
                                }
                                """.formatted(brand.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("FREE"))
                .andExpect(jsonPath("$.type").value("4K Ultra HD"));
    }

    private Film saveFilm(String title, double price) {
        return saveFilm(title, price, "Brand " + title, FilmRegion.B);
    }

    private Film saveFilm(String title, double price, String brandName, FilmRegion region) {
        Brand brand = brandRepository.save(new Brand(brandName));
        Film film = new Film();
        film.setTitle(title);
        film.setGenre("Drama");
        film.setDirector("Director");
        film.setCountry("Japan");
        film.setRegion(region);
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
