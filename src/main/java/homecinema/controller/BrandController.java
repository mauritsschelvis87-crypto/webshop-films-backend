package homecinema.controller;

import homecinema.model.Brand;
import homecinema.repository.BrandRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "https://s1156856.student.inf.st.hsleiden.nl"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandRepository brandRepository;

    public BrandController(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @GetMapping
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @PostMapping
    public Brand createBrand(@RequestBody Brand brand) {
        return brandRepository.save(brand);
    }
}
