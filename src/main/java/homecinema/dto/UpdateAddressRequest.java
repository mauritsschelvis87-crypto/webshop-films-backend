package homecinema.dto;

import lombok.Data;

@Data
public class UpdateAddressRequest {
    private String email;
    private String street;
    private String postalCode;
    private String city;
    private String country;
}
