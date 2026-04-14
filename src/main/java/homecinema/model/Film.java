package homecinema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
public class Film extends BaseEntity {

    private String title;
    private String genre;
    private String director;
    private String country;
    private int year;
    private int runtime;
    private String type;
    private double price;
    private String imageUrl;
    private String trailerUrl;
    private String aspectRatio;
    private String colorOrBlackAndWhite;
    private boolean silent;
    private int weight;

    @Column(length = 2000)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Actor> actors;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ElementCollection
    @CollectionTable(name = "film_stills", joinColumns = @JoinColumn(name = "film_id"))
    @Column(name = "still_url")
    private List<String> stills;
}
