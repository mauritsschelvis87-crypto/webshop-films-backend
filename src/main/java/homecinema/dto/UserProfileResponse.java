package homecinema.dto;

import homecinema.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String role;
    private Address address;
}
