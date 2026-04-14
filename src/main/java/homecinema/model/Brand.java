package homecinema.model;

import jakarta.persistence.Entity;

@Entity
public class Brand extends NamedEntity {

    public Brand() {}

    public Brand(String name) {
        super(name);
    }
}
