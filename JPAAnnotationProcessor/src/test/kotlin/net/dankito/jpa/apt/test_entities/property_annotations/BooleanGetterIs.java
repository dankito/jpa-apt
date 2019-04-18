package net.dankito.jpa.apt.test_entities.property_annotations;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class BooleanGetterIs {

    @Id
    private String id;

    @Column
    private boolean valid;


    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

}
