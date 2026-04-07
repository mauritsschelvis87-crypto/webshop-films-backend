package homecinema.controller;

import homecinema.dto.UpdateAddressRequest;
import homecinema.dto.UserProfileResponse;
import homecinema.model.Address;
import homecinema.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:4200", "https://s1156856.student.inf.st.hsleiden.nl"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserRepository userRepository;

    public AccountController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getAccount(@AuthenticationPrincipal UserDetails userDetails) {
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

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateAccount(@AuthenticationPrincipal UserDetails userDetails,
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
