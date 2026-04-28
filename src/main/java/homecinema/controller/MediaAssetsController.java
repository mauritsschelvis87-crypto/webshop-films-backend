package homecinema.controller;

import homecinema.config.CorsOrigins;
import homecinema.dto.MediaAssetsResponse;
import homecinema.service.MediaAssetsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(originPatterns = {CorsOrigins.LOCALHOST_4200, CorsOrigins.VERCEL_APP, CorsOrigins.SCHOOL_FRONTEND}, allowCredentials = "true")
@RestController
@RequestMapping("/api/media-assets")
public class MediaAssetsController {

    private final MediaAssetsService mediaAssetsService;

    public MediaAssetsController(MediaAssetsService mediaAssetsService) {
        this.mediaAssetsService = mediaAssetsService;
    }

    @GetMapping
    public MediaAssetsResponse getMediaAssets() {
        return mediaAssetsService.getMediaAssets();
    }
}
