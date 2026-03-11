package homecinema.controller;

import homecinema.dto.UpdateAddressRequest;
import homecinema.dto.UserProfileResponse;
import homecinema.model.Address;
import homecinema.model.User;
import homecinema.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PutMapping
    public ResponseEntity<User> updateAccount(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody UpdateAddressRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : request.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return userRepository.findByEmail(email).map(user -> {
            Address address = new Address();
            address.setStreet(request.getStreet());
            address.setPostalCode(request.getPostalCode());
            address.setCity(request.getCity());
            address.setCountry(request.getCountry());

            user.setAddress(address);
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> ResponseEntity.ok(new UserProfileResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getRole(),
                        user.getAddress()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/address")
    public ResponseEntity<UserProfileResponse> updateMyAccount(@AuthenticationPrincipal UserDetails userDetails,
                                                               @RequestBody UpdateAddressRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findByEmail(userDetails.getUsername()).map(user -> {
            Address address = new Address();
            address.setStreet(request.getStreet());
            address.setPostalCode(request.getPostalCode());
            address.setCity(request.getCity());
            address.setCountry(request.getCountry());

            user.setAddress(address);
            userRepository.save(user);

            return ResponseEntity.ok(new UserProfileResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole(),
                    user.getAddress()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }
}
