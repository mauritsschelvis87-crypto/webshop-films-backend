package homecinema.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private String username;
    private double totalPrice;
    private String giftCardCode;
    private String giftCode;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long productId;
        private int quantity;
    }
}
