package homecinema.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Director extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String birthPlace;
    private Integer birthYear;
    private Integer deathYear;
    private String image;
    @Column(length = 1000)
    private String infoLine;
    @Column(length = 1000)
    private String bornLine;
    @Column(length = 1000)
    private String diedLine;
    @ElementCollection
    private List<DirectorPerson> people;

    @Column(length = 4000)
    private String bio;

    @Column(length = 1000)
    private String education;
}
