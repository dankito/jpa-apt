package net.dankito.jpa.apt.test_entities.inheritance_annotations;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class EntityWithoutSuperclass {

    @Id
    private String id;

}
