package net.dankito.jpa.apt.test_entities.class_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = EntityAndTableAnnotationNameSet.ENTITY_NAME)
@Table(name = TableAnnotationNameSet.TABLE_NAME)
public class EntityAndTableAnnotationNameSet {

    public static final String ENTITY_NAME = "entity";

    public static final String TABLE_NAME = "table";


    @Id
    private String id;

}
