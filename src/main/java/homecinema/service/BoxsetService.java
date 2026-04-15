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
    private List<BoxsetResponse> cachedBoxsets;

    public BoxsetService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

            return objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load boxsets.json.", exception);
        }
    }

    private boolean matchesSlug(String boxsetSlug, String routeSlug) {
        return normalize(boxsetSlug).equals(normalize(routeSlug));
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
