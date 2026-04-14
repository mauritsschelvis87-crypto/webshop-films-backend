package homecinema.model;

import jakarta.persistence.Entity;

@Entity
public class Actor extends NamedEntity {

    public Actor() {}

    public Actor(String name) {
        super(name);
    }
}
