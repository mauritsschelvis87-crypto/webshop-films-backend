package homecinema.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CloudinaryAssetResolverTest {

    private final CloudinaryAssetResolver resolver =
            new CloudinaryAssetResolver(new ObjectMapper(), "cloudinary-test");

    @Test
    void returnsOriginalPathWhenNoMappingExists() {
        String assetPath = "/assets/covers/non-existent.jpg";

        assertEquals(assetPath, resolver.resolveCover(assetPath));
    }

    @Test
    void returnsRemoteUrlForConfiguredDirectorMapping() {
        String assetPath = "/assets/directors/akira_kurosawa.jpg";
        String mappedUrl = "https://res.cloudinary.com/demo/image/upload/homecinema/directors/akira_kurosawa.jpg";

        assertEquals(mappedUrl, resolver.resolveDirector(assetPath));
    }

    @Test
    void returnsRemoteUrlForConfiguredBoxsetMappingWithoutLeadingSlash() {
        String assetPath = "assets/boxset/bergman_box_2.jpg";
        String mappedUrl = "https://res.cloudinary.com/demo/image/upload/homecinema/boxset/bergman_box_2.jpg";

        assertEquals(mappedUrl, resolver.resolveBoxset(assetPath));
    }

    @Test
    void returnsEmptyStringWhenAssetIsExplicitlyLeftUnmapped() {
        String assetPath = "/assets/stills/missing.jpg";

        assertEquals("", resolver.resolveStill(assetPath));
    }

    @Test
    void returnsRemoteUrlForConfiguredGiftMapping() {
        String assetPath = "/assets/gifts/digital-10.png";
        String mappedUrl = "https://res.cloudinary.com/demo/image/upload/homecinema/gifts/10_euro.png";

        assertEquals(mappedUrl, resolver.resolveGift(assetPath));
    }
}
