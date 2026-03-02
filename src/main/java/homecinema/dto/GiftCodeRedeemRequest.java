package homecinema.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GiftCodeRedeemRequest {
    private String code;
    private Long filmId;
    private String redeemerEmail;
}
