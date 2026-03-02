package homecinema.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GiftCodeResponse {
    private String code;
    private String recipientEmail;
    private String allowedType;
    private boolean redeemed;
    private Long redeemedFilmId;
}
