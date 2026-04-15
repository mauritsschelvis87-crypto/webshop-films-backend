package homecinema.dto;

import java.util.List;

public record BoxsetResponse(
        Long id,
        String slug,
        String title,
        String subtitle,
        String topImage,
        String secondaryImage,
        String description,
        List<BoxsetSpecResponse> specs,
        List<BoxsetMediaItemResponse> mediaItems,
        BoxsetProductResponse product
) {
    public record BoxsetSpecResponse(
            String label,
            String value
    ) {}

    public record BoxsetMediaItemResponse(
            String type,
            String url
    ) {}

    public record BoxsetProductResponse(
            Long id,
            String title,
            String genre,
            String director,
            String country,
            String region,
            int year,
            int runtime,
            double price,
            String imageUrl,
            String trailerUrl,
            String aspectRatio,
            String colorOrBlackAndWhite,
            String description,
            BrandResponse brand,
            String type,
            Integer weight,
            List<String> stills,
            boolean silent
    ) {}

    public record BrandResponse(
            Long id,
            String name
    ) {}
}
