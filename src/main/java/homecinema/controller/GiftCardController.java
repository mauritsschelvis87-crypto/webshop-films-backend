package homecinema.controller;

import homecinema.config.CorsOrigins;
import homecinema.model.GiftCard;
import homecinema.service.GiftCardService;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(originPatterns = {CorsOrigins.LOCALHOST_4200, CorsOrigins.VERCEL_APP, CorsOrigins.SCHOOL_FRONTEND}, allowCredentials = "true")
@RestController
@RequestMapping("/api/giftcards")

public class GiftCardController {

    private final GiftCardService giftCardService;

    public GiftCardController(GiftCardService giftCardService) {
        this.giftCardService = giftCardService;
    }

    @PostMapping("/create")
    public GiftCard create(@RequestParam String code, @RequestParam Double amount) {
        return giftCardService.createGiftCard(code, amount);
    }

    @PostMapping("/redeem")
    public GiftCard redeem(@RequestParam String code, @RequestParam Long userId) {
        return giftCardService.redeemGiftCard(code, userId);
    }
}
