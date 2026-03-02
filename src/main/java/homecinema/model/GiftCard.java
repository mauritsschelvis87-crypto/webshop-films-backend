package homecinema.model;

import homecinema.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gift_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne
    @JoinColumn(name = "used_by_id")
    private User usedBy;
}
