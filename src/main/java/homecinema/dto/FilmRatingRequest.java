package homecinema.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FilmRatingRequest {
    private BigDecimal rating;
}
