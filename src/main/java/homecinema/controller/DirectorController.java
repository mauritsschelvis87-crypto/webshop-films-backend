package homecinema.controller;

import homecinema.config.CorsOrigins;
import homecinema.model.Director;
import homecinema.service.DirectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(originPatterns = {CorsOrigins.LOCALHOST_4200, CorsOrigins.VERCEL_APP, CorsOrigins.SCHOOL_FRONTEND}, allowCredentials = "true")
@RestController
@RequestMapping("/api/directors")
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorService.findAll();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Director> getDirectorBySlug(@PathVariable String slug) {
        Director director = directorService.findBySlug(slug);
        if (director == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(director);
    }
}
