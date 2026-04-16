package homecinema.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CloudinaryAssetResolver {

    public enum AssetCategory {
        COVERS("covers_cloudinary.json"),
        STILLS("stills_cloudinary.json"),
        DIRECTORS("directors_cloudinary.json"),
        BOXSET("boxset_cloudinary.json"),
        GIFTS("gifts_cloudinary.json");

        private final String resourceName;

        AssetCategory(String resourceName) {
            this.resourceName = resourceName;
        }

        public String getResourceName() {
            return resourceName;
        }
    }

    private final Map<AssetCategory, Map<String, String>> mappingsByCategory;
    private final String resourcePrefix;

    @Autowired
    public CloudinaryAssetResolver(ObjectMapper objectMapper) {
        this(objectMapper, "");
    }

    CloudinaryAssetResolver(ObjectMapper objectMapper, String resourcePrefix) {
        this.resourcePrefix = resourcePrefix == null ? "" : resourcePrefix.trim();
        this.mappingsByCategory = loadMappings(objectMapper);
    }

    public String resolveCover(String assetPath) {
        return resolve(AssetCategory.COVERS, assetPath);
    }

    public String resolveStill(String assetPath) {
        return resolve(AssetCategory.STILLS, assetPath);
    }

    public String resolveDirector(String assetPath) {
        return resolve(AssetCategory.DIRECTORS, assetPath);
    }

    public String resolveBoxset(String assetPath) {
        return resolve(AssetCategory.BOXSET, assetPath);
    }

    public String resolveGift(String assetPath) {
        return resolve(AssetCategory.GIFTS, assetPath);
    }

    public Map<String, String> getMappings(AssetCategory category) {
        return mappingsByCategory.getOrDefault(category, Map.of());
    }

    private String resolve(AssetCategory category, String assetPath) {
        String normalizedPath = normalizePath(assetPath);
        if (!StringUtils.hasText(normalizedPath) || isRemoteUrl(normalizedPath)) {
            return assetPath;
        }

        Map<String, String> mappings = getMappings(category);
        if (mappings.isEmpty()) {
            return assetPath;
        }

        for (String candidate : new String[]{
                normalizedPath,
                stripLeadingSlash(normalizedPath),
                ensureLeadingSlash(normalizedPath),
                extractFileName(normalizedPath)
        }) {
            if (mappings.containsKey(candidate)) {
                return mappings.get(candidate);
            }
        }

        return assetPath;
    }

    private Map<AssetCategory, Map<String, String>> loadMappings(ObjectMapper objectMapper) {
        Map<AssetCategory, Map<String, String>> loadedMappings = new EnumMap<>(AssetCategory.class);

        for (AssetCategory category : AssetCategory.values()) {
            loadedMappings.put(category, loadCategoryMappings(objectMapper, category));
        }

        return Map.copyOf(loadedMappings);
    }

    private Map<String, String> loadCategoryMappings(ObjectMapper objectMapper, AssetCategory category) {
        try (InputStream inputStream = getClass().getResourceAsStream("/" + resourcePath(category))) {
            if (inputStream == null) {
                return Map.of();
            }

            Map<String, String> rawMappings = objectMapper.readValue(inputStream, new TypeReference<>() {});
            Map<String, String> sanitizedMappings = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : rawMappings.entrySet()) {
                String key = normalizePath(entry.getKey());
                String value = entry.getValue() == null ? "" : entry.getValue().trim();

                if (StringUtils.hasText(key)) {
                    sanitizedMappings.put(key, value);
                }
            }

            return Map.copyOf(sanitizedMappings);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + category.getResourceName(), exception);
        }
    }

    private String resourcePath(AssetCategory category) {
        return resourcePrefix.isEmpty()
                ? category.getResourceName()
                : resourcePrefix + "/" + category.getResourceName();
    }

    private boolean isRemoteUrl(String value) {
        return value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("//")
                || value.startsWith("data:");
    }

    private String normalizePath(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        return value.trim().replace('\\', '/');
    }

    private String stripLeadingSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }

    private String ensureLeadingSlash(String value) {
        return value.startsWith("/") ? value : "/" + value;
    }

    private String extractFileName(String value) {
        int lastSlashIndex = value.lastIndexOf('/');
        return lastSlashIndex >= 0 ? value.substring(lastSlashIndex + 1) : value;
    }
}
