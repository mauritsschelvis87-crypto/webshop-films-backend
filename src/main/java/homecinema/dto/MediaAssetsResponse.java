package homecinema.dto;

import java.util.Map;

public record MediaAssetsResponse(
        Map<String, String> covers,
        Map<String, String> stills,
        Map<String, String> directors,
        Map<String, String> boxset,
        Map<String, String> gifts
) {
}
