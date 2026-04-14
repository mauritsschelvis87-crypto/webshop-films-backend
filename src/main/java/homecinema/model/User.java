package homecinema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true)
    private String username;
    private String password;
    private String role;
    private String email;
    private String name;

    @Embedded
    private Address address;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    private List<Film> wishlist = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_film_ratings", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "film_id")
    @Column(name = "rating")
    private Map<Long, Integer> filmRatings = new HashMap<>();

}
