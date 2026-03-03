package homecinema.controller;

import homecinema.dto.GiftCodeCreateRequest;
import homecinema.dto.GiftCodeRedeemRequest;
import homecinema.dto.GiftCodeResponse;
import homecinema.model.Film;
import homecinema.model.GiftCode;
import homecinema.service.FilmService;
import homecinema.service.GiftCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/gift-codes")
public class GiftCodeController {

    private final GiftCodeService giftCodeService;
    private final FilmService filmService;

    public GiftCodeController(GiftCodeService giftCodeService, FilmService filmService) {
        this.giftCodeService = giftCodeService;
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<GiftCodeResponse> createGiftCode(@RequestBody GiftCodeCreateRequest request) {
        LocalDateTime expiration = LocalDateTime.now().plusYears(2);
        GiftCode giftCode = giftCodeService.createGiftCode(
                request.getRecipientEmail(),
                request.getAllowedType(),
                expiration,
                request.getAddress()
        );

        GiftCodeResponse response = toResponse(giftCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<GiftCodeResponse> getGiftCode(@PathVariable String code) {
        Optional<GiftCode> giftCodeOpt = giftCodeService.findByCode(code);
        if (giftCodeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(giftCodeOpt.get()));
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemGiftCode(@RequestBody GiftCodeRedeemRequest request) {
        Optional<GiftCode> giftCodeOpt = giftCodeService.findByCode(request.getCode());
        if (giftCodeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid gift code");
        }

        GiftCode giftCode = giftCodeOpt.get();

        if (giftCode.isRedeemed()) {
            return ResponseEntity.badRequest().body("Gift code already redeemed");
        }

        if (giftCode.getExpirationDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Gift code expired");
        }

        if (!giftCode.getAllowedType().equalsIgnoreCase(
                filmService.findById(request.getFilmId()).getType())) {
            return ResponseEntity.badRequest().body("Film type not allowed for this gift code");
        }

        Film film = filmService.findById(request.getFilmId());
        if (film == null) {
            return ResponseEntity.badRequest().body("Invalid film");
        }

        GiftCode redeemed = giftCodeService.redeemCode(giftCode, film, request.getRedeemerEmail());
        return ResponseEntity.ok(toResponse(redeemed));
    }

    private GiftCodeResponse toResponse(GiftCode giftCode) {
        GiftCodeResponse res = new GiftCodeResponse();
        res.setCode(giftCode.getCode());
        res.setRecipientEmail(giftCode.getRecipientEmail());
        res.setAllowedType(giftCode.getAllowedType());
        res.setRedeemed(giftCode.isRedeemed());
        res.setRedeemedFilmId(giftCode.getRedeemedFilm() != null ? giftCode.getRedeemedFilm().getId() : null);
        return res;
    }
}
