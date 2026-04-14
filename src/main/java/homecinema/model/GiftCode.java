package homecinema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "gift_codes")
public class GiftCode extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String allowedType;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    private boolean redeemed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "redeemed_film_id")
    private Film redeemedFilm;

    private String redeemedByEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    private Address address;
}
