package net.dankito.jpa.apt.test_entities.class_annotations;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class AccessAnnotationSetToField {

    @Id
    private String id;

}
