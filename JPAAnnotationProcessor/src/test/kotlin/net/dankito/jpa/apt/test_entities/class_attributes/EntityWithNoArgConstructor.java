package net.dankito.jpa.apt.test_entities.class_attributes;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class EntityWithNoArgConstructor {

    @Id
    private String id;

}
