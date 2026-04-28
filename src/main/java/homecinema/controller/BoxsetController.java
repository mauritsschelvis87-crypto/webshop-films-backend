package homecinema.controller;

import homecinema.config.CorsOrigins;
import homecinema.dto.BoxsetResponse;
import homecinema.service.BoxsetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(originPatterns = {CorsOrigins.LOCALHOST_4200, CorsOrigins.VERCEL_APP, CorsOrigins.SCHOOL_FRONTEND}, allowCredentials = "true")
@RestController
@RequestMapping("/api/boxsets")
public class BoxsetController {
    private final BoxsetService boxsetService;

    public BoxsetController(BoxsetService boxsetService) {
        this.boxsetService = boxsetService;
    }

    @GetMapping
    public List<BoxsetResponse> getAllBoxsets() {
        return boxsetService.findAll();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BoxsetResponse> getBoxsetBySlug(@PathVariable String slug) {
        BoxsetResponse boxset = boxsetService.findBySlug(slug);
        if (boxset == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(boxset);
    }
}
