package net.dankito.jpa.apt.test_entities.property_annotations;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
@Access(AccessType.PROPERTY)
public class AccessAnnotationSetToProperty_IdAnnotationOnField {

    @Id
    private String id;


    public String getId() {
        return id;
    }

}
