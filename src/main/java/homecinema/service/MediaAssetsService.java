package homecinema.service;

import homecinema.dto.MediaAssetsResponse;
import org.springframework.stereotype.Service;

@Service
public class MediaAssetsService {

    private final CloudinaryAssetResolver cloudinaryAssetResolver;

    public MediaAssetsService(CloudinaryAssetResolver cloudinaryAssetResolver) {
        this.cloudinaryAssetResolver = cloudinaryAssetResolver;
    }

    public MediaAssetsResponse getMediaAssets() {
        return new MediaAssetsResponse(
                cloudinaryAssetResolver.getMappings(CloudinaryAssetResolver.AssetCategory.COVERS),
                cloudinaryAssetResolver.getMappings(CloudinaryAssetResolver.AssetCategory.STILLS),
                cloudinaryAssetResolver.getMappings(CloudinaryAssetResolver.AssetCategory.DIRECTORS),
                cloudinaryAssetResolver.getMappings(CloudinaryAssetResolver.AssetCategory.BOXSET),
                cloudinaryAssetResolver.getMappings(CloudinaryAssetResolver.AssetCategory.GIFTS)
        );
    }
}
