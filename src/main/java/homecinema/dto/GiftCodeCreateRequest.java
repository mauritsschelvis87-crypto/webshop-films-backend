package homecinema.dto;

import homecinema.model.Address;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GiftCodeCreateRequest {
    private String recipientEmail;
    private String allowedType;
    private Address address;
}