package homecinema.service;

import homecinema.model.GiftCard;
import homecinema.model.Order;
import homecinema.model.User;
import homecinema.repository.GiftCardRepository;
import homecinema.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final UserRepository userRepository;

    public GiftCardService(GiftCardRepository giftCardRepository, UserRepository userRepository) {
        this.giftCardRepository = giftCardRepository;
        this.userRepository = userRepository;
    }

    public GiftCard createGiftCard(String code, Double amount) {
        GiftCard gc = new GiftCard();
        gc.setCode(code);
        gc.setAmount(amount);
        gc.setUsed(false);
        return giftCardRepository.save(gc);
    }

    public Optional<GiftCard> findByCode(String code) {
        return giftCardRepository.findByCode(code);
    }

    public GiftCard getUsableGiftCard(String code) {
        GiftCard card = giftCardRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Giftcard code not found"));

        if (card.isUsed()) {
            throw new RuntimeException("Giftcard already used");
        }

        return card;
    }

    public GiftCard redeemGiftCard(String code, Long userId) {
        GiftCard card = getUsableGiftCard(code);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        card.setUsed(true);
        card.setUsedBy(user);

        return giftCardRepository.save(card);
    }

    public GiftCard applyGiftCardToOrder(GiftCard card, User user, Order order) {
        card.setUsed(true);
        card.setUsedBy(user);
        return giftCardRepository.save(card);
    }
}
