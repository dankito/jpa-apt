package net.dankito.jpa.apt.test_entities.property_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class AccessAnnotationUnset_IdAnnotationOnGetter {

    private String id;


    @Id
    public String getId() {
        return id;
    }

}
