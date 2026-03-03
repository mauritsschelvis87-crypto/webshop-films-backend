package homecinema.controller;

import homecinema.model.Address;
import homecinema.model.User;
import homecinema.repository.UserRepository;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserRepository userRepository;

    public AccountController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Ophalen van gebruiker op basis van email (frontend gebruikt localStorage 'username' = email)
     * URL: GET /api/account?email=gebruiker@example.com
     */
    @GetMapping
    public ResponseEntity<User> getAccount(@RequestParam("email") String email) {
        System.out.println("GET /api/account ontvangen met email: " + email);

        var userOpt = userRepository.findByEmail(email);
        System.out.println("Gebruiker gevonden? " + userOpt.isPresent());

        return userOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Getter
    public static class UpdateAddressRequest {
        private String email;
        private String street;
        private String postalCode;
        private String city;
        private String country;

        public void setEmail(String email) { this.email = email; }
        public void setStreet(String street) { this.street = street; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public void setCity(String city) { this.city = city; }
        public void setCountry(String country) { this.country = country; }
    }

    @PutMapping
    public ResponseEntity<User> updateAccount(@RequestBody UpdateAddressRequest request) {
        System.out.println("PUT /api/account ontvangen:");
        System.out.println("- Email: " + request.getEmail());
        System.out.println("- Straat: " + request.getStreet());
        System.out.println("- Postcode: " + request.getPostalCode());
        System.out.println("- Stad: " + request.getCity());
        System.out.println("- Land: " + request.getCountry());

        return userRepository.findByEmail(request.getEmail()).map(user -> {
            Address address = new Address();
            address.setStreet(request.getStreet());
            address.setPostalCode(request.getPostalCode());
            address.setCity(request.getCity());
            address.setCountry(request.getCountry());

            user.setAddress(address);
            userRepository.save(user);

            System.out.println("Adres succesvol bijgewerkt voor gebruiker: " + user.getEmail());
            return ResponseEntity.ok(user);
        }).orElseGet(() -> {
            System.out.println("Gebruiker niet gevonden voor email: " + request.getEmail());
            return ResponseEntity.notFound().build();
        });
    }
}
