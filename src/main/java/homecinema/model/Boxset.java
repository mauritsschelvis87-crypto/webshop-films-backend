package homecinema.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Set;

@Getter
@Entity
public class Boxset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private double price;
    private String imageUrl;

    @Column(length = 2000)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "boxset_films",
            joinColumns = @JoinColumn(name = "boxset_id"),
            inverseJoinColumns = @JoinColumn(name = "film_id")
    )
    private Set<Film> films;
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(double price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setFilms(Set<Film> films) { this.films = films; }
}
