package net.dankito.jpa.apt.test_entities.class_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EntityAnnotationNameUnset {

    @Id
    private String id;

}
