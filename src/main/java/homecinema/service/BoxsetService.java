package homecinema.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import homecinema.dto.BoxsetResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Service
public class BoxsetService {
    private final ObjectMapper objectMapper;
    private final CloudinaryAssetResolver cloudinaryAssetResolver;
    private List<BoxsetResponse> cachedBoxsets;

    public BoxsetService(ObjectMapper objectMapper, CloudinaryAssetResolver cloudinaryAssetResolver) {
        this.objectMapper = objectMapper;
        this.cloudinaryAssetResolver = cloudinaryAssetResolver;
    }

    public List<BoxsetResponse> findAll() {
        if (cachedBoxsets == null) {
            cachedBoxsets = loadBoxsets();
        }

        return cachedBoxsets;
    }

    public BoxsetResponse findBySlug(String slug) {
        return findAll().stream()
                .filter(boxset -> matchesSlug(boxset.slug(), slug))
                .findFirst()
                .orElse(null);
    }

    private List<BoxsetResponse> loadBoxsets() {
        try (InputStream inputStream = getClass().getResourceAsStream("/boxsets.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not find boxsets.json in backend resources.");
            }

            List<BoxsetResponse> boxsets = objectMapper.readValue(inputStream, new TypeReference<>() {});
            return boxsets.stream()
                    .map(this::resolveAssetUrls)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load boxsets.json.", exception);
        }
    }

    private boolean matchesSlug(String boxsetSlug, String routeSlug) {
        return normalize(boxsetSlug).equals(normalize(routeSlug));
    }

    private BoxsetResponse resolveAssetUrls(BoxsetResponse boxset) {
        return new BoxsetResponse(
                boxset.id(),
                boxset.slug(),
                boxset.title(),
                boxset.subtitle(),
                cloudinaryAssetResolver.resolveBoxset(boxset.topImage()),
                cloudinaryAssetResolver.resolveBoxset(boxset.secondaryImage()),
                boxset.description(),
                boxset.specs(),
                boxset.mediaItems().stream()
                        .map(mediaItem -> "image".equalsIgnoreCase(mediaItem.type())
                                ? new BoxsetResponse.BoxsetMediaItemResponse(
                                        mediaItem.type(),
                                        cloudinaryAssetResolver.resolveBoxset(mediaItem.url())
                                )
                                : mediaItem)
                        .toList(),
                resolveProductAssets(boxset.product())
        );
    }

    private BoxsetResponse.BoxsetProductResponse resolveProductAssets(BoxsetResponse.BoxsetProductResponse product) {
        if (product == null) {
            return null;
        }

        return new BoxsetResponse.BoxsetProductResponse(
                product.id(),
                product.title(),
                product.genre(),
                product.director(),
                product.country(),
                product.region(),
                product.year(),
                product.runtime(),
                product.price(),
                cloudinaryAssetResolver.resolveBoxset(product.imageUrl()),
                product.trailerUrl(),
                product.aspectRatio(),
                product.colorOrBlackAndWhite(),
                product.description(),
                product.brand(),
                product.type(),
                product.weight(),
                product.stills() == null
                        ? List.of()
                        : product.stills().stream()
                        .map(cloudinaryAssetResolver::resolveBoxset)
                        .toList(),
                product.silent()
        );
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
