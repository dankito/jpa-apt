package net.dankito.jpa.apt.test_entities.class_attributes;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class EntityWithoutNoArgConstructor {

    @Id
    private String id;


    public EntityWithoutNoArgConstructor(Object iDontProvideANoArgConstructor) {

    }

}
