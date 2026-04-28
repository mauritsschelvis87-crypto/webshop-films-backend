package homecinema.dto;

import homecinema.model.enums.FilmRegion;
import lombok.Data;

import java.math.BigDecimal; // Importeer BigDecimal
import java.util.List;

@Data
public class FilmDTO {
    private Long id;
    private String title;
    private String genre;
    private String director;
    private String country;
    private FilmRegion region;
    private int year;
    private int runtime;
    private double price;
    private String imageUrl;
    private String trailerUrl;
    private String aspectRatio;
    private String colorOrBlackAndWhite;
    private boolean silent;
    private String description;
    private BrandDTO brand;
    private String format;
    private String type;
    private List<String> stills;
    private BigDecimal userRating; // Bestaande persoonlijke rating
    private BigDecimal averageCommunityRating; // Nieuw: gemiddelde community rating
    private Integer communityRatingCount; // Nieuw: aantal community ratings
}
