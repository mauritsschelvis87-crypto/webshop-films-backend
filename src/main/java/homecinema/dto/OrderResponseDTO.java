package homecinema.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private String number;
    private String status;
    private LocalDateTime orderDate;
    private double subtotalPrice;
    private double discountAmount;
    private double totalPrice;
    private String appliedGiftCardCode;
    private String appliedGiftCode;
    private List<OrderItemDTO> orderItems;

    @Data
    public static class OrderItemDTO {
        private FilmDTO film;
        private int quantity;
        private double price;
    }

    @Data
    public static class FilmDTO {
        private Long id;
        private String title;
        private double price;
        private String imageUrl;
        private String type;
        private String director;
        private String country;
        private int year;
        private int runtime;
        private String aspectRatio;
        private String colorOrBlackAndWhite;
        private String description;
        private List<String> stills;
        private BrandDTO brand;
        private FormatDTO format;
    }

    @Data
    public static class BrandDTO {
        private Long id;
        private String name;
    }

    @Data
    public static class FormatDTO {
        private Long id;
        private String name;
    }
}
