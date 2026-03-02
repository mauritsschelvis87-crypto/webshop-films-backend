package homecinema.service;

import homecinema.model.Address;
import homecinema.model.Film;
import homecinema.model.GiftCode;
import homecinema.repository.GiftCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GiftCodeService {

    private final GiftCodeRepository giftCodeRepository;

    public GiftCodeService(GiftCodeRepository giftCodeRepository) {
        this.giftCodeRepository = giftCodeRepository;
    }

    public GiftCode createGiftCode(String recipientEmail, String allowedType, LocalDateTime expirationDate, Address address) {
        GiftCode giftCode = new GiftCode();
        giftCode.setCode(generateUniqueCode());
        giftCode.setRecipientEmail(recipientEmail);
        giftCode.setAllowedType(allowedType);
        giftCode.setExpirationDate(expirationDate);
        giftCode.setCreatedAt(LocalDateTime.now());
        giftCode.setRedeemed(false);
        giftCode.setAddress(address);

        return giftCodeRepository.save(giftCode);
    }

    public Optional<GiftCode> findByCode(String code) {
        return giftCodeRepository.findByCode(code);
    }

    public GiftCode redeemCode(GiftCode giftCode, Film redeemedFilm, String redeemerEmail) {
        giftCode.setRedeemed(true);
        giftCode.setRedeemedFilm(redeemedFilm);
        giftCode.setRedeemedByEmail(redeemerEmail);
        return giftCodeRepository.save(giftCode);
    }

    private String generateUniqueCode() {
        // Simpele unieke code generator; kan uitgebreid worden
        return java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
