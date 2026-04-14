package homecinema.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class OrderItem extends BaseEntity {
    // Getters and setters
    private int quantity;
    private double price;

    @ManyToOne
    @JsonBackReference
    private Order order;

    @ManyToOne
    private Film film;

    @ManyToOne
    private Boxset boxset;
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
    public void setOrder(Order order) { this.order = order; }
    public void setFilm(Film film) { this.film = film; }
    public void setBoxset(Boxset boxset) { this.boxset = boxset; }
}
