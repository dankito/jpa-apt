package net.dankito.jpa.apt.test_entities.class_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = EntityAnnotationNameSet.ENTITY_NAME)
public class EntityAnnotationNameSet {

    public static final String ENTITY_NAME = "entity";


    @Id
    private String id;

}
