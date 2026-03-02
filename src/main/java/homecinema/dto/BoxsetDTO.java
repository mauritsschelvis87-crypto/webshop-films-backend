package homecinema.dto;

import lombok.Data;

@Data
public class BoxsetDTO {
    private Long id;
    private String title;
    private double price;
    private String imageUrl;
}
