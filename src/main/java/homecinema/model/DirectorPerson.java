package homecinema.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class DirectorPerson {
    private String name;
    private String birthDate;
    private String birthPlace;
    private Integer birthYear;
    private String deathDate;
    private Integer deathYear;
}
