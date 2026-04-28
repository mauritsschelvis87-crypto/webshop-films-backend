package homecinema.controller;

import homecinema.config.CorsOrigins;
import homecinema.dto.AuthRequest;
import homecinema.dto.RegisterRequest;
import homecinema.dto.UserProfileResponse;
import homecinema.repository.UserRepository;
import homecinema.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {CorsOrigins.LOCALHOST_4200, CorsOrigins.VERCEL_APP, CorsOrigins.SCHOOL_FRONTEND}, allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Ongeldige inloggegevens");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
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
}
